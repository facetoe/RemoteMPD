package org.a0z.mpd;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import com.google.gson.Gson;
import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.event.TrackPositionListener;
import org.a0z.mpd.exception.NoBluetoothServerConnectionException;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.*;


public class BluetoothConnection {
    // Unique UUID for this application
    private static final UUID MY_UUID = UUID.fromString("04c6093b-0000-1000-8000-00805f9b34fb");
    private static final String TAG = "BluetoothConnection";

    // Constants that indicate the current connection state
    private static final int STATE_NONE = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private static int CURRENT_STATE = STATE_NONE;

    private final BluetoothAdapter bluetoothAdapter;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private ArrayList<ConnectionListener> connectionListeners = new ArrayList<ConnectionListener>();

    LinkedBlockingQueue<MPDResponse> syncedResultQueue = new LinkedBlockingQueue<MPDResponse>();
    LinkedBlockingQueue<MPDResponse> mpdChangeQueue = new LinkedBlockingQueue<MPDResponse>();

    ExecutorService pool = Executors.newFixedThreadPool(10);


    // For sending JSON across the wire
    private final Gson gson = new Gson();
    private final BluetoothDevice bluetoothDevice;

    /**
     * Constructor. Prepares a new BluetoothConnection session.
     */
    public BluetoothConnection(BluetoothDevice device) {
        bluetoothDevice = device;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void addConnectionListener(ConnectionListener listener) {
        connectionListeners.add(listener);
    }

    private void notifyConnectionFailed(String message) {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.connectionFailed(message);
        }
    }

    private void notifyConnectionSucceded(String message) {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.connectionSucceeded(message);
        }
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     */
    public synchronized void connect() {
        if (bluetoothAdapter == null) {
            notifyConnectionFailed("Bluetooth is not supported");
            return;
        }

        Log.i(TAG, "connecting to: " + bluetoothDevice);

        // Cancel connection if we are attempting to connect
        if (CURRENT_STATE == STATE_CONNECTING) {
            Log.w(TAG, "Connection was already in progress, cancelling");
            if (connectThread != null) {
                connectThread.cancel();
                connectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        // Start the thread to connect with the given device
        connectThread = new ConnectThread(bluetoothDevice);
        connectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Stop all threads
     */
    public synchronized void disconnect() {
        Log.d(TAG, "disconnect()");
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        setState(STATE_NONE);
    }

    public boolean isConnected() {
        return connectedThread != null && connectedThread.isAlive();
    }

    public void sendCommand(BTServerCommand command) throws NoBluetoothServerConnectionException {
        if (CURRENT_STATE != STATE_CONNECTED) {
            throw new NoBluetoothServerConnectionException("No connection to Bluetooth Server");
        }
        try {
            String commandJSON = gson.toJson(command);
            write(commandJSON);
        } catch (IOException e) {
            throw new NoBluetoothServerConnectionException("Failed to send command to Bluetooth server");
        }
    }

    public Future<MPDResponse> syncedWriteRead(BTServerCommand command) {
        Log.d(TAG, "Sending synced command: " + command);
        command.setSynchronous(true);
        BTServerCallable callable = new BTServerCallable(command);
        return pool.submit(callable);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param data The data to write
     */
    private void write(String data) throws IOException {
        Log.d("Writing: ", data);
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            r = connectedThread;
        }
        // Perform the write unsynchronized
        r.write(data);
    }

    private void bluetoothConnectionFailed(String message) {
        setState(STATE_NONE);
        notifyConnectionFailed(message);
    }

    private synchronized void setState(int newState) {
        Log.d(TAG, "setState() " + CURRENT_STATE + " -> " + newState);
        CURRENT_STATE = newState;
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     */
    synchronized void spawnConnectedThread(BluetoothSocket socket) {
        Log.d(TAG, "spawnConnectedThread()");

        // Cancel the thread that completed the connection
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();

        setState(STATE_CONNECTED);
        notifyConnectionSucceded("");
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "run()");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            bluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                bluetoothConnectionFailed(e.getMessage());
                Log.e(TAG, "Bluetooth Connection failed");

                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothConnection.this) {
                connectThread = null;
            }

            // Start the ConnectedThread thread
            spawnConnectedThread(mmSocket);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final BufferedReader inputReader;
        private final BufferedWriter outputWriter;
        private boolean isCanceled = false;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread()");
            bluetoothSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = bluetoothSocket.getInputStream();
                tmpOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            inputReader = new BufferedReader(new InputStreamReader(tmpIn, Charset.forName("UTF-8")));
            outputWriter = new BufferedWriter(new OutputStreamWriter(tmpOut, Charset.forName("UTF-8")));
        }

        public void run() {
            Log.d(TAG, "ConnectedThread running");
            String input;

            // Keep listening to the InputStream while connected with the Server
            while (!isCanceled) {
                try {
                    // Messages from the server are terminated with a newline.
                    input = inputReader.readLine();
                    handleMessage(input);

                } catch (IOException e) {
                    Log.e(TAG, "disconnected");
                    bluetoothConnectionFailed(e.getMessage());
                    break;
                }
            }
        }

        private void handleMessage(String message) {
            MPDResponse response = gson.fromJson(message, MPDResponse.class);
            if (response.isSynchronous()) {
                addToSyncedResultQueue(response);
            } else if(response.getResponseType() == MPDResponse.EVENT_UPDATE_RAW_CHANGES) {
                addToMPDChangeQueue(response);
            }
        }

        private void addToMPDChangeQueue(MPDResponse response) {
            try {
                Log.i(TAG, "Adding changes to queue");
                mpdChangeQueue.put(response);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void addToSyncedResultQueue(MPDResponse response) {
            try {
                Log.i(TAG, "Putting command in queue");
                syncedResultQueue.put(response);
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted while adding response to queue");
            }
        }

        public void write(String data) throws IOException {
            outputWriter.write(data + "\n");
            outputWriter.flush();
        }

        public void cancel() {
            isCanceled = true;
            innerDisconnect();
        }

        private void innerDisconnect() {
            try {
                bluetoothSocket.close();
                inputReader.close();
                outputWriter.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    class BTServerCallable extends BTServerCommand implements Callable<MPDResponse> {
        public BTServerCallable(BTServerCommand command) {
            super(command.getCommand(), command.getArgs(), command.isSynchronous());
        }

        @Override
        public MPDResponse call() throws Exception {
            sendCommand(this);
            return syncedResultQueue.take();
        }
    }
}
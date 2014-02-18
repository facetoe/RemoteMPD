package com.facetoe.remotempd;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import com.facetoe.remotempd.exceptions.NoBluetoothServerConnectionException;
import com.facetoe.remotempd.helpers.SettingsHelper;
import com.facetoe.remotempd.listeners.ConnectionListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.a0z.mpd.Music;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;


public class BluetoothConnection {
    // Unique UUID for this application
    private static final UUID MY_UUID = UUID.fromString("04c6093b-0000-1000-8000-00805f9b34fb");
    private static final String TAG = RMPDApplication.APP_PREFIX + "BluetoothConnection";

    // Constants that indicate the current connection state
    private static final int STATE_NONE = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private static int CURRENT_STATE = STATE_NONE;

    private final BluetoothAdapter bluetoothAdapter;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private ConnectionListener connectionListener;

    private List<BTServerCommand> commandQueue = new ArrayList<BTServerCommand>();
    ArrayBlockingQueue<MPDResponse> syncedCommandQueue;

    // For sending JSON across the wire
    private final Gson gson = new Gson();
    private final BluetoothMPDStatusMonitor monitor;

    /**
     * Constructor. Prepares a new BluetoothConnection session.
     */
    public BluetoothConnection(BluetoothMPDStatusMonitor monitor,
                               ArrayBlockingQueue<MPDResponse> syncedCommandQueue,
                               ConnectionListener listener) {

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.syncedCommandQueue = syncedCommandQueue;
        this.monitor = monitor;
        this.connectionListener = listener;
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     */
    public synchronized void connect() {
        if (bluetoothAdapter == null) {
            connectionListener.connectionFailed("Bluetooth is not supported");
            return;
        }

        String lastDevice = SettingsHelper.getLastDevice();
        if (lastDevice.isEmpty()) {
            connectionListener.connectionFailed("No Bluetooth device selected");
            Log.w(TAG, "No device selected");
            return;
        }

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(lastDevice);
        Log.i(TAG, "connecting to: " + device);

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
        connectThread = new ConnectThread(device);
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

    public void queueCommand(String command, String... args) {
        queueCommand(new BTServerCommand(command, args));
    }

    private void queueCommand(BTServerCommand command) {
        commandQueue.add(command);
    }

    public void sendCommandQueue() throws NoBluetoothServerConnectionException {
        sendCommandQueue(false);
    }

    public void sendCommandQueue(boolean withSeparator) throws NoBluetoothServerConnectionException {
        String commandstr;

        if (withSeparator) {
            commandstr = BTServerCommand.MPD_CMD_START_BULK_OK + "\n";
        } else {
            commandstr = BTServerCommand.MPD_CMD_START_BULK + "\n";
        }

        for (BTServerCommand command : commandQueue) {
            commandstr += command.toString();
        }
        commandstr += BTServerCommand.MPD_CMD_END_BULK + "\n";
        commandQueue = new ArrayList<BTServerCommand>();
        sendCommand(new BTServerCommand(commandstr));
    }

    public void sendCommand(String command, String... args) throws NoBluetoothServerConnectionException {
        sendCommand(new BTServerCommand(command, args));
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
        try {
            command.setSynchronous(true);
            BTServerCallable callable = new BTServerCallable(command);
            ExecutorService pool = Executors.newFixedThreadPool(2);
            Future<MPDResponse> result = pool.submit(callable);
            Log.i(TAG, "Before get");
            //List<String> c = extractStringList(result.get());
            Log.i(TAG, "After get");
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error in syncedWriteRead", e);
            return null;
        }
    }

    private List<String> extractStringList(MPDResponse response) {
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(response.getObjectJSON(0), listType);
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

    private void bluetoothConnectionFailed() {
        setState(STATE_NONE);
        connectionListener.connectionFailed("Failed to connect to Bluetooth server");
    }

    private void connectionLost() {
        setState(STATE_NONE);
        connectionListener.connectionFailed("Lost connection to the Bluetooth server");
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
        connectionListener.connectionSucceeded("");
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
                bluetoothConnectionFailed();

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
                    connectionLost();
                    break;
                }
            }
        }

        private void handleMessage(String message) {
            MPDResponse response = gson.fromJson(message, MPDResponse.class);
            if(response.isSynchronous()) {
                try {
                    Log.i(TAG, "Putting command in queue");
                    syncedCommandQueue.put(response);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Interrupted while adding response to queue");
                }
            } else {
                Log.i(TAG, "monitor.handleMessage");
                monitor.handleMessage(response);
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
            Log.i(TAG, "Sending: " + this.getCommand());
            sendCommand(this);
            MPDResponse response = syncedCommandQueue.take();
            return response;
        }
    }
}
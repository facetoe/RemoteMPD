package com.facetoe.remotempd;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import com.facetoe.remotempd.exceptions.NoBluetoothServerConnectionException;
import com.facetoe.remotempd.listeners.ConnectionListener;
import com.google.gson.Gson;
import org.a0z.mpd.MPDCommand;

import java.io.*;
import java.util.UUID;


public class BluetoothController implements ConnectionListener {
    // Unique UUID for this application
    private static final UUID MY_UUID = UUID.fromString("04c6093b-0000-1000-8000-00805f9b34fb");
    protected static final String TAG = RemoteMPDApplication.APP_PREFIX + "BluetoothController";

    // Constants that indicate the current connection state
    protected static final int STATE_NONE = 0;       // we're doing nothing
    protected static final int STATE_CONNECTING = 1; // now initiating an outgoing connection
    protected static final int STATE_CONNECTED = 2;  // now spawnConnectedThread to a remote device
    protected static int CURRENT_STATE = STATE_NONE;

    // Member fields
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice device;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private RemoteMPDApplication app = RemoteMPDApplication.getInstance();

    // For sending JSON across the wire
    private Gson gson = new Gson();
    BluetoothMPDStatusMonitor monitor;

    /**
     * Constructor. Prepares a new BluetoothController session.
     */
    public BluetoothController(BluetoothMPDStatusMonitor monitor) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.monitor = monitor;
    }

    @Override
    public void connectionFailed(String message) {
        Log.w(TAG, "Connection failed in BluetoothController: " + message);
        app.notifyConnectionFailed(message);
    }

    @Override
    public void connectionSucceeded(String message) {
        Log.i(TAG, "Connection succeeded in BluetoothController: " + message );
        app.notifyConnectionSucceeded(message);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     */
    public synchronized void connect() {
        String lastDevice = app.getSettings().getLastDevice();
        if(lastDevice.isEmpty()) {
            connectionFailed("No Bluetooth device selected");
            Log.w(TAG, "No device selected");
            return;
        }

        device = bluetoothAdapter.getRemoteDevice(lastDevice);
        Log.i(TAG, "connecting to: " + device);

        // Cancel any thread attempting to make a connection
        if (CURRENT_STATE == STATE_CONNECTING) {
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

    public void sendCommand(MPDCommand command) throws NoBluetoothServerConnectionException {
        if(CURRENT_STATE != STATE_CONNECTED) {
            throw new NoBluetoothServerConnectionException("No connection to Bluetooth Server");
        }
        write(command.toString());
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param data The data to write
     */
    private void write(String data) {
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
        connectionFailed("Failed to connect to Bluetooth server");
    }

    private void connectionLost() {
        setState(STATE_NONE);
        connectionFailed("Lost connection to the Bluetooth server");
    }

    private synchronized void setState(int newState) {
        Log.d(TAG, "setState() " + CURRENT_STATE + " -> " + newState);
        CURRENT_STATE = newState;
    }

    public synchronized int getState() { return CURRENT_STATE; }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     */
    public synchronized void spawnConnectedThread(BluetoothSocket socket) {
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
        connectionSucceeded("");
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
            synchronized (BluetoothController.this) {
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
        private final BluetoothSocket mmSocket;
        private BufferedReader inputReader;
        private PrintWriter outputWriter;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread()");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            inputReader = new BufferedReader(new InputStreamReader(tmpIn));
            outputWriter = new PrintWriter(new BufferedOutputStream(tmpOut), true);
        }

        public void run() {
            Log.d(TAG, "ConnectedThread running");
            String input;

            // Keep listening to the InputStream while spawnConnectedThread
            while (true) {
                try {
                    // Messages are terminated with a newline
                    // so this should read the whole message.
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
            Log.i(TAG, "Message Received: " + message);
            MPDResponse response = gson.fromJson(message, MPDResponse.class);
            monitor.handleMessage(response);
        }

        // Write to the output stream. The writer will flush the stream automatically
        public void write(String data) {
            outputWriter.println(data);
        }

        public void cancel() {
            try {
                inputReader.close();
                outputWriter.close();
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
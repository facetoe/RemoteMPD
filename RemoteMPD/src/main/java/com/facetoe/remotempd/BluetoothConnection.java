package com.facetoe.remotempd;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import com.facetoe.remotempd.exceptions.NoBluetoothServerConnectionException;
import com.facetoe.remotempd.helpers.SettingsHelper;
import com.facetoe.remotempd.listeners.ConnectionListener;
import com.google.gson.Gson;
import org.a0z.mpd.AbstractCommand;
import org.a0z.mpd.MPDCommand;

import java.io.*;
import java.nio.charset.Charset;
import java.util.UUID;


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

    // For sending JSON across the wire
    private final Gson gson = new Gson();
    private final BluetoothMPDStatusMonitor monitor;

    /**
     * Constructor. Prepares a new BluetoothConnection session.
     */
    public BluetoothConnection(BluetoothMPDStatusMonitor monitor, ConnectionListener listener) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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
        if(lastDevice.isEmpty()) {
            connectionListener.connectionFailed("No Bluetooth device selected");
            Log.w(TAG, "No device selected");
            return;
        }

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(lastDevice);
        Log.i(TAG, "connecting to: " + device);

        // Don't connect if we are already connecting
        if (CURRENT_STATE == STATE_CONNECTING) {
            Log.w(TAG, "Connection already in progress");
            return;
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
        Log.d(TAG, "Really disconnected");
        setState(STATE_NONE);
    }

    public boolean isConnected() {
        return connectedThread != null && connectedThread.isAlive();
    }

    public void sendCommand(AbstractCommand command) throws NoBluetoothServerConnectionException {
        if(CURRENT_STATE != STATE_CONNECTED) {
            throw new NoBluetoothServerConnectionException("No connection to Bluetooth Server");
        }
        try {
            write(command.toString());
        } catch (IOException e) {
            throw new NoBluetoothServerConnectionException("Failed to send command to Bluetooth server");
        }
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
            while (true) {
                try {
                    // Messages from the server are terminated with a newline.
                    long startTime = System.nanoTime();
                    input = inputReader.readLine();
                    long endTime = System.nanoTime();
                    Log.d(TAG, String.format("Read %d bytes in %.3f seconds",
                            input.getBytes().length,
                            (endTime - startTime) / 1000000000.0F));

                    handleMessage(input);

                } catch (IOException e) {
                    Log.e(TAG, "disconnected");
                    connectionLost();
                    break;
                }
            }
        }

        private void handleMessage(String message) {
            Log.v(TAG, "Message Received: " + message);
            MPDResponse response = gson.fromJson(message, MPDResponse.class);
            monitor.handleMessage(response);
        }

        public void write(String data) throws IOException {
            // Append a newline here as the server uses them to determine the end of commands.
            outputWriter.write(data + "\n");
            outputWriter.flush();
        }

        public void cancel() {
            try {
                bluetoothSocket.close();
                inputReader.close();
                outputWriter.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
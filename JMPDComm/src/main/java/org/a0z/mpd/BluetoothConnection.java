package org.a0z.mpd;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import com.google.gson.Gson;
import org.a0z.mpd.exception.BluetoothServerException;

import java.io.*;
import java.nio.charset.Charset;
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

    // Thread used to communicate with the bluetooth server
    private BluetoothCommThread bluetoothCommThread;

    // Command responses from the bluetooth server are placed in this queue
    private final LinkedBlockingQueue<MPDResponse> syncedResultQueue = new LinkedBlockingQueue<MPDResponse>();

    // MPD idle connection updates are placed in this queue
    final LinkedBlockingQueue<MPDResponse> mpdIdleUpdateQueue = new LinkedBlockingQueue<MPDResponse>();

    // Thread pool for executing BTServerCallable's
    private final ExecutorService pool = Executors.newFixedThreadPool(10);

    // For sending JSON across the wire
    private final Gson gson = new Gson();

    // The device to connect to
    private final BluetoothDevice bluetoothDevice;

    /**
     * Initialize and connect to the bluetooth server.
     *
     * @param device The device to connect to
     * @throws org.a0z.mpd.exception.BluetoothServerException On failure to connect.
     */
    public BluetoothConnection(BluetoothDevice device) throws BluetoothServerException {
        bluetoothDevice = device;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        connect();
    }

    /**
     * Attempt to connect to the device and launch the BluetoothCommThread for managing the connection.
     */
    public synchronized void connect() throws BluetoothServerException {
        if (bluetoothAdapter == null) {
            throw new BluetoothServerException("Bluetooth is not supported");
        }

        if (CURRENT_STATE == STATE_CONNECTING) {
            Log.w(TAG, "Connection was already in progress, canceling");
            if (bluetoothCommThread != null) {
                bluetoothCommThread.disconnect();
            }
        }
        Log.i(TAG, "connecting to: " + bluetoothDevice);

        BluetoothSocket btSocket = getAndConnectBluetoothSocket();

        // Start the thread to communicate with bluetooth server
        bluetoothCommThread = new BluetoothCommThread(btSocket);
        bluetoothCommThread.start();
    }


    /**
     * Attempt to connect to the remote device and open a BluetoothSocket.
     *
     * @return The connected BluetoothSocket
     * @throws org.a0z.mpd.exception.BluetoothServerException If we can't find the device (most likely) or other error.
     */
    private BluetoothSocket getAndConnectBluetoothSocket() throws BluetoothServerException {
        BluetoothSocket socket;
        setState(STATE_CONNECTING);
        try {
            socket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);

            // Cancel discovery as it slows down connection
            bluetoothAdapter.cancelDiscovery();

            // Attempt to connect.
            socket.connect();
        } catch (IOException e) {
            Log.e(TAG, "Bluetooth connection failed: " + e.getMessage());
            throw new BluetoothServerException("Failed to connect to bluetooth server");
        }
        return socket;
    }

    /**
     * Send a command to the Bluetooth server.
     *
     * @param command The command to send.
     * @throws org.a0z.mpd.exception.BluetoothServerException On failure to send command.
     */
    void sendCommand(BTServerCommand command) throws BluetoothServerException {
        if (CURRENT_STATE != STATE_CONNECTED) {
            throw new BluetoothServerException("No connection to Bluetooth Server");
        }
        try {
            String commandJSON = gson.toJson(command);
            bluetoothCommThread.write(commandJSON);
        } catch (IOException e) {
            throw new BluetoothServerException("Failed to send command to Bluetooth server");
        }
    }

    /**
     * Sends a command and returns a Future<MPDResponse> object for use in the calling thread.
     *
     * @param command The command to send.
     * @return A Future<MPDResponse> object.
     */
    public Future<MPDResponse> syncedWriteRead(BTServerCommand command) {
        Log.d(TAG, "Sending synced command: " + command);
        command.setSynchronous(true);
        BTServerCallable callable = new BTServerCallable(command);
        return pool.submit(callable);
    }

    /**
     * This class submits a command and waits for the response to be added to the syncedResultQueue.
     * Once the response is received the call() method will return allowing the caller to access the result.
     */
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

    /**
     * Kill the connection to the Bluetooth Server.
     */
    public synchronized void disconnect() {
        Log.d(TAG, "disconnect()");
        if (bluetoothCommThread != null) {
            bluetoothCommThread.disconnect();
            bluetoothCommThread = null;
        }
        setState(STATE_NONE);
    }

    public boolean isConnected() {
        return bluetoothCommThread != null && bluetoothCommThread.isAlive() && CURRENT_STATE == STATE_CONNECTED;
    }

    private synchronized void setState(int newState) {
        Log.d(TAG, "setState() " + CURRENT_STATE + " -> " + newState);
        CURRENT_STATE = newState;
    }

    /**
     * This thread handles all communication with the Bluetooth server.
     */
    private class BluetoothCommThread extends Thread {
        private final BluetoothSocket socket;
        private BufferedReader inputReader;
        private BufferedWriter outputWriter;

        private boolean isCanceled = false;

        public BluetoothCommThread(BluetoothSocket socket) {
            this.socket = socket;
        }

        public void run() {
            setName("BluetoothCommThread");
            String input;
            initStreams();
            setState(STATE_CONNECTED);
            while (!isCanceled) {
                try {
                    Log.v(TAG, "Waiting for response from server");

                    // Messages from the server are terminated with a newline.
                    input = inputReader.readLine();
                    handleMessage(input);

                } catch (IOException e) {
                    Log.e(TAG, "disconnected");
                    setState(STATE_NONE);

                    // Close the socket
                    try {
                        socket.close();
                    } catch (IOException e2) {
                        Log.e(TAG, "unable to close() socket during connection failure", e2);
                    }
                    break;
                }
            }
        }

        /**
         * Get the BluetoothSocket input and output streams and initialize Buffered Reader/Writer
         */
        private void initStreams() {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            inputReader = new BufferedReader(new InputStreamReader(tmpIn, Charset.forName("UTF-8")));
            outputWriter = new BufferedWriter(new OutputStreamWriter(tmpOut, Charset.forName("UTF-8")));
        }

        /**
         * Handle the messages received from the bluetooth server and add them to
         * the appropriate queue.
         *
         * @param message The message to send.
         */
        private void handleMessage(String message) {
            MPDResponse response = gson.fromJson(message, MPDResponse.class);

            if (response.getResponseType() == MPDResponse.EVENT_ERROR) {
                Log.e(TAG, "Error received from bluetooth server, adding to syncedResultQueue");
                addToSyncedResultQueue(response);

            } else if (response.isSynchronous()) {
                addToSyncedResultQueue(response);

            } else if (response.getResponseType() == MPDResponse.EVENT_UPDATE_RAW_CHANGES) {
                addToMpdIdleUpdateQueue(response);

            } else {
                Log.e(TAG, "Received strange command: " + response.getResponseType());
            }
        }

        private void addToMpdIdleUpdateQueue(MPDResponse response) {
            try {
                Log.v(TAG, "Adding MpdIdleUpdate to the mpdIdleUpdateQueue");
                mpdIdleUpdateQueue.put(response);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void addToSyncedResultQueue(MPDResponse response) {
            try {
                Log.d(TAG, "Adding synced response to the syncedResultQueue: " + response);
                syncedResultQueue.put(response);
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted while adding response to queue");
            }
        }

        /**
         * Send the data to the server. Note, the server expects messages to be terminated with a newline.
         *
         * @param data The data to write.
         * @throws IOException On error.
         */
        public void write(String data) throws IOException {
            outputWriter.write(data + "\n");
            outputWriter.flush();
        }


        /**
         * Cleanup and disconnect from the bluetooth server.
         */
        public void disconnect() {
            isCanceled = true;
            try {
                if (socket != null) {
                    socket.close();
                }
                if (inputReader != null) {
                    inputReader.close();
                }
                if (outputWriter != null) {
                    outputWriter.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}


package com.facetoe.RemoteMPD;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.google.gson.Gson;
import org.a0z.mpd.MPDCommand;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothCommandService extends CommandService implements CommandServiceController {

    // Unique UUID for this application
    private static final UUID MY_UUID = UUID.fromString("04c6093b-0000-1000-8000-00805f9b34fb");

    // Member fields
    private BluetoothAdapter mAdapter;
    private BluetoothDevice device;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private RemoteMPDApplication app;

    // For sending JSON across the wire
    private Gson gson = new Gson();


    /**
     * Constructor. Prepares a new BluetoothCommandService session.
     *
     * @param context The UI Activity Context
     * @param handler A Handler to send messages back to the UI Activity
     * @param device The BluetoothDevice to connect
     */
    public BluetoothCommandService(Context context, Handler handler, BluetoothDevice device) {
        super(context, handler);
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        app = RemoteMPDApplication.getInstance();
        this.device = device;
    }


    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     */
    @Override
    public synchronized void connect() {
        Log.d(TAG, "connect to: " + device);
        // Cancel any thread attempting to make a connection
        if (CURRENT_STATE == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);

    }

    /**
     * Stop all threads
     */
    @Override
    public synchronized void stop() {
        Log.d(TAG, "stop()");
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        setState(STATE_NONE);
    }

    @Override
    public boolean isConnected() {
        return mConnectedThread != null && mConnectedThread.isAlive();
    }

    @Override
    public void sendCommand(MPDCommand command) {
        write(command.toString());
    }

    @Override
    public void handleMPDResponse(MPDResponse response) {
        Message msg = handler.obtainMessage(MESSAGE_RECEIVED);
        Bundle bundle = new Bundle();
        bundle.putSerializable(RemoteMPDActivity.MESSAGE, response);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    /**
     * Send a string to the server.
     *
     * @param message The responseType to write.
     */
    public void write(String message) {
        write(message.getBytes(Charset.forName("UTF-8")));
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    private void write(byte[] out) {
        Log.d("Writing: ", new String(out));
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (CURRENT_STATE != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     */
    public synchronized void spawnConnectedThread(BluetoothSocket socket) {
        Log.d(TAG, "spawnConnectedThread()");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        setState(STATE_CONNECTED);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */

    //TODO fix this to reconnect
    @Override
    protected void connectionLost() {
////        if (mConnectionLostCount < 3) {
////            if(D) Log.i(TAG, "Connection lost");
////            // Send a reconnect responseType back to the Activity
////            Message msg = mHandler.obtainMessage(RemoteBluetooth.MESSAGE_TOAST);
////            Bundle bundle = new Bundle();
////            bundle.putString(RemoteBluetooth.TOAST, "Device connection was lost. Reconnecting " + mConnectionLostCount);
////            msg.setData(bundle);
////            mHandler.sendMessage(msg);
////            connect(mSavedDevice);
////
////        } else {
//        setState(STATE_LISTEN);
//        // Send a failure responseType back to the Activity
//        Message msg = mHandler.obtainMessage(RemoteBluetooth.MESSAGE_TOAST);
//        Bundle bundle = new Bundle();
//        bundle.putString(RemoteBluetooth.TOAST, "Device connection was lost");
//        msg.setData(bundle);
//        mHandler.sendMessage(msg);
////        }
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
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
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                Log.e(TAG, "Exception in connectThread: ", e);
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothCommandService.this) {
                mConnectThread = null;
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
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

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

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            Log.e(TAG, "State: " + getState() + " : " + CURRENT_STATE);
        }

        public void run() {
            Log.d(TAG, "ConnectedThread running");
            int bytesRead;

            if (app.getSongList() == null) {
                sendCommand(new MPDCommand(MPDCommand.MPD_CMD_PLAYLIST_CHANGES, "-1"));
            }

            StringBuilder builder = new StringBuilder();
            // Keep listening to the InputStream while spawnConnectedThread
            while (true) {
                try {
                    // Read from the InputStream
                    int ch;
                    bytesRead = 0;
                    while ((ch = mmInStream.read()) != 10) {
                        bytesRead++;
                        builder.append((char) ch);
                    }
                    Log.i(TAG, "READ " + bytesRead + " bytes.");
                    handleMessage(builder.toString());
                    builder.setLength(0);

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
            handleMPDResponse(response);
        }

        public void write(String message) {
            write((message + "\n").getBytes(Charset.forName("UTF-8")));
        }

        /**
         * Write to the spawnConnectedThread OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "write() failed", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
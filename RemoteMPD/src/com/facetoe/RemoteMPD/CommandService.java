package com.facetoe.RemoteMPD;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import org.a0z.mpd.MPDCommand;

/**
 * Created by facetoe on 1/01/14.
 */

interface CommandServiceController {
    void connect();
    void stop();
    boolean isConnected();
    void sendCommand(MPDCommand command);
    void handleMPDResponse(MPDResponse response);
}

public class CommandService {
    protected static final String TAG = "CommandService";
    protected Context context;
    protected Handler handler;

    // Constants that indicate the current connection state
    protected static final int STATE_NONE = 0;       // we're doing nothing
    protected static final int STATE_CONNECTING = 1; // now initiating an outgoing connection
    protected static final int STATE_CONNECTED = 2;  // now spawnConnectedThread to a remote device
    protected static int CURRENT_STATE = STATE_NONE;

    protected static final int MESSAGE_RECEIVED = 3;

    public CommandService(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    protected void connectionFailed() {
        setState(STATE_NONE);
    }

    protected void connectionLost() {
        setState(STATE_NONE);
    }

    protected synchronized void setState(int newState) {
        Log.d(TAG, "setState() " + CURRENT_STATE + " -> " + newState);
        CURRENT_STATE = newState;
    }

    public synchronized int getState() { return CURRENT_STATE; }
}

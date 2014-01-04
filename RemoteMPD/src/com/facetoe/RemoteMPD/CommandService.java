package com.facetoe.RemoteMPD;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import org.a0z.mpd.MPDCommand;

/**
 * Created by facetoe on 1/01/14.
 */


public class CommandService {
    protected static final String TAG = "CommandService";

    protected static final int MESSAGE_RECEIVED = 3;
    protected static final int ERROR_RECIEVED = 4;

    public CommandService() {
    }
}

package com.facetoe.RemoteMPD;

import android.util.Log;
import org.a0z.mpdlocal.MPDCommand;

class BluetoothMPDManager extends PlayerManager {
    private static final String TAG = "BluetoothMPDManager";
    public BluetoothMPDManager(RemoteMPDCommandService service) {
        super(service);
    }

    @Override
    public void play() {
        Log.d(TAG, MPDCommand.MPD_CMD_PLAY);
        sendCommand(MPDCommand.MPD_CMD_PLAY);
    }

    @Override
    public void stop() {
        Log.d(TAG, MPDCommand.MPD_CMD_STOP);
        sendCommand(MPDCommand.MPD_CMD_STOP);
    }

    @Override
    public void pause() {
        Log.d(TAG, MPDCommand.MPD_CMD_PAUSE);
        sendCommand(MPDCommand.MPD_CMD_PAUSE);
    }

    @Override
    public void next() {
        Log.d(TAG, "Next");
        sendCommand(MPDCommand.MPD_CMD_NEXT);
    }

    @Override
    public void prev() {
        Log.d(TAG, MPDCommand.MPD_CMD_PREV);
        sendCommand(MPDCommand.MPD_CMD_PREV);
    }

    @Override
    public void setVolume(int newVolume) {
        if(newVolume < 0 || newVolume > 100) return;
        String command = new MPDCommand(
                MPDCommand.MPD_CMD_VOLUME,
                Integer.toString(newVolume))
                .toString();
        Log.d(TAG, command);
        sendCommand(command);
    }

    private void sendCommand(String message) {
        commandService.send(message + "\n");
    }
}
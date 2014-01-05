package com.facetoe.RemoteMPD;

import android.util.Log;
import org.a0z.mpd.MPDCommand;

class BluetoothMPDManager implements MPDPlayerController {
    private static final String TAG = RemoteMPDApplication.APP_TAG;
    private BluetoothController controller;
    private RemoteMPDApplication app = RemoteMPDApplication.getInstance();

    public BluetoothMPDManager() {
        controller = app.getBluetoothController();
    }

    @Override
    public void start() {
        if(!controller.isConnected())
            controller.connect();
    }

    @Override
    public void restart() {
        controller.disconnect();
        controller.connect();
    }

    @Override
    public void disconnect() {
        controller.disconnect();
    }

    @Override
    public void play() {
        Log.d(TAG, MPDCommand.MPD_CMD_PLAY);
        controller.sendCommand(new MPDCommand(MPDCommand.MPD_CMD_PLAY));
    }

    @Override
    public void playID(int id) {
        controller.sendCommand(new MPDCommand(MPDCommand.MPD_CMD_PLAY_ID, Integer.toString(id)));
    }

    @Override
    public void stop() {
        Log.d(TAG, MPDCommand.MPD_CMD_STOP);
        controller.sendCommand(new MPDCommand(MPDCommand.MPD_CMD_STOP));
    }

    @Override
    public void pause() {
        Log.d(TAG, MPDCommand.MPD_CMD_PAUSE);
        controller.sendCommand(new MPDCommand(MPDCommand.MPD_CMD_PAUSE));
    }

    @Override
    public void next() {
        Log.d(TAG, MPDCommand.MPD_CMD_NEXT);
        controller.sendCommand(new MPDCommand(MPDCommand.MPD_CMD_NEXT));
    }

    @Override
    public void prev() {
        Log.d(TAG, MPDCommand.MPD_CMD_PREV);
        controller.sendCommand(new MPDCommand(MPDCommand.MPD_CMD_PREV));
    }

    @Override
    public void setVolume(int newVolume) {
        if (newVolume < 0 || newVolume > 100) return;
        MPDCommand command = new MPDCommand(
                MPDCommand.MPD_CMD_VOLUME,
                Integer.toString(newVolume));
        controller.sendCommand(command);
        Log.d(TAG, command.toString());
    }
}
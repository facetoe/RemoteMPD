package com.facetoe.RemoteMPD;

import android.os.Handler;
import android.util.Log;
import org.a0z.mpd.MPDCommand;

class BluetoothMPDManager implements MPDPlayerController {
    private static final String TAG = "BluetoothMPDManager";



    private BluetoothController commandService;
    private RemoteMPDApplication app = RemoteMPDApplication.getInstance();

    public BluetoothMPDManager() {
        commandService = new BluetoothController();
    }

    @Override
    public void start() {
        commandService.connect();
    }

    @Override
    public void play() {
        Log.d(TAG, MPDCommand.MPD_CMD_PLAY);
        commandService.sendCommand(new MPDCommand(MPDCommand.MPD_CMD_PLAY));
    }

    @Override
    public void playID(int id) {
        commandService.sendCommand(new MPDCommand(MPDCommand.MPD_CMD_PLAY_ID, Integer.toString(id)));
    }

    @Override
    public void stop() {
        Log.d(TAG, MPDCommand.MPD_CMD_STOP);
        commandService.sendCommand(new MPDCommand(MPDCommand.MPD_CMD_STOP));
    }

    @Override
    public void pause() {
        Log.d(TAG, MPDCommand.MPD_CMD_PAUSE);
        commandService.sendCommand(new MPDCommand(MPDCommand.MPD_CMD_PAUSE));
    }

    @Override
    public void next() {
        Log.d(TAG, MPDCommand.MPD_CMD_NEXT);
        commandService.sendCommand(new MPDCommand(MPDCommand.MPD_CMD_NEXT));
    }

    @Override
    public void prev() {
        Log.d(TAG, MPDCommand.MPD_CMD_PREV);
        commandService.sendCommand(new MPDCommand(MPDCommand.MPD_CMD_PREV));
    }

    @Override
    public void setVolume(int newVolume) {
        if(newVolume < 0 || newVolume > 100) return;
        MPDCommand command = new MPDCommand(
                MPDCommand.MPD_CMD_VOLUME,
                Integer.toString(newVolume));
        commandService.sendCommand(command);
        Log.d(TAG, command.toString());
    }
}
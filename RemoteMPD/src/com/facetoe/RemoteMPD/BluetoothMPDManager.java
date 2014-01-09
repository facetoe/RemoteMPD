package com.facetoe.RemoteMPD;

import android.util.Log;
import org.a0z.mpd.MPDCommand;
import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.event.TrackPositionListener;

class BluetoothMPDManager extends AbstractMPDManager {
    private static final String TAG = RemoteMPDApplication.APP_TAG;
    private BluetoothController controller;
    private BluetoothMPDStatusMonitor bluetoothMonitor;
    private static BluetoothMPDManager instance;

    private BluetoothMPDManager() {
        bluetoothMonitor = new BluetoothMPDStatusMonitor();
        controller = new BluetoothController(bluetoothMonitor);
    }

    public static BluetoothMPDManager getInstance() {
        if(instance == null)
            instance = new BluetoothMPDManager();
        return instance;
    }

    @Override
    public void connect() {
        if(!controller.isConnected())
            controller.connect();
    }

    @Override
    public boolean isConnected() {
        return controller.isConnected();
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

    @Override
    public void addStatusChangeListener(StatusChangeListener listener) {
        bluetoothMonitor.addStatusChangeListener(listener);
    }

    @Override
    public void removeStatusChangeListener(StatusChangeListener listener) {
        bluetoothMonitor.removeStatusChangeListener(listener);
    }

    @Override
    public void addTrackPositionListener(TrackPositionListener listener) {
        bluetoothMonitor.addTrackPositionListener(listener);
    }

    @Override
    public void removeTrackPositionListener(TrackPositionListener listener) {
        bluetoothMonitor.removeTrackPositionListener(listener);
    }
}
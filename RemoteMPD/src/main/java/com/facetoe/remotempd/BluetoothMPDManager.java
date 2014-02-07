package com.facetoe.remotempd;

import android.util.Log;
import org.a0z.mpd.MPDCommand;
import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.event.TrackPositionListener;

class BluetoothMPDManager extends AbstractMPDManager {
    private static final String TAG = RMPDApplication.APP_PREFIX + "BluetoothMPDManager";

    private final BluetoothConnection btConnection;
    private final BluetoothMPDStatusMonitor bluetoothMonitor;

    public BluetoothMPDManager() {
        bluetoothMonitor = new BluetoothMPDStatusMonitor();
        btConnection = new BluetoothConnection(bluetoothMonitor);
    }

    @Override
    protected void connectInternal() {
        if (!btConnection.isConnected()) {
            btConnection.connect();
        }
    }

    @Override
    public boolean isConnected() {
        return btConnection.isConnected();
    }

    @Override
    public void disconnect() {
        btConnection.disconnect();
        Log.i(TAG, "Disconnected from bluetooth: " + !btConnection.isConnected());
    }

    @Override
    public void sendCommand(MPDCommand command) {
        try {
            btConnection.sendCommand(command);
            Log.i(TAG, "Sent command: " + command);
        } catch (Exception e) {
            btConnection.connectionFailed(e.getMessage());
        }
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
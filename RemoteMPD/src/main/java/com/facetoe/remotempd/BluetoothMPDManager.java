package com.facetoe.remotempd;

import android.util.Log;
import org.a0z.mpd.MPDCommand;
import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.event.TrackPositionListener;

class BluetoothMPDManager extends AbstractMPDManager {
    private static final String TAG = RemoteMPDApplication.APP_PREFIX + "BluetoothMPDManager";
    private BluetoothConnection btConnection;
    private BluetoothMPDStatusMonitor bluetoothMonitor;

    public BluetoothMPDManager() {
        bluetoothMonitor = new BluetoothMPDStatusMonitor();
        btConnection = new BluetoothConnection(bluetoothMonitor);
    }

    @Override
    public void connect() {
        if (!btConnection.isConnected()) {
            RemoteMPDApplication.getInstance().showConnectingProgressDialog();
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
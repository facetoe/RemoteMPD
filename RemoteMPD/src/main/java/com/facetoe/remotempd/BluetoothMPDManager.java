package com.facetoe.remotempd;

import android.util.Log;
import com.facetoe.remotempd.exceptions.NoBluetoothServerConnectionException;
import org.a0z.mpd.MPDCommand;
import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.event.TrackPositionListener;

class BluetoothMPDManager extends AbstractMPDManager {
    private static final String TAG = RemoteMPDApplication.APP_PREFIX + "BluetoothMPDManager";
    private BluetoothController controller;
    private BluetoothMPDStatusMonitor bluetoothMonitor;

    public BluetoothMPDManager() {
        bluetoothMonitor = new BluetoothMPDStatusMonitor();
        controller = new BluetoothController(bluetoothMonitor);
    }

    @Override
    public void connect() {
        if (!controller.isConnected())
            controller.connect();
    }

    @Override
    public boolean isConnected() {
        return controller.isConnected();
    }

    @Override
    public void disconnect() {
        controller.disconnect();
        Log.i(TAG, "Disconnected from bluetooth: " + !controller.isConnected());
    }

    @Override
    public void sendCommand(MPDCommand command) {
        lastCommand = command;
        try {
            controller.sendCommand(command);
            Log.i(TAG, "Sending command: " + command);
            retryAttempts = 0;
        } catch (Exception e) {
            if (retryAttempts < MAX_COMMAND_RETRY_ATTEMPTS) {
                attemptReconnect();
            } else {
                handleError(e);
            }
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
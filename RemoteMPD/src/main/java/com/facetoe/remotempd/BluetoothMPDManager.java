package com.facetoe.remotempd;

import android.util.Log;
import com.facetoe.remotempd.listeners.ConnectionListener;
import org.a0z.mpd.AbstractCommand;
import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.event.TrackPositionListener;

class BluetoothMPDManager extends AbstractMPDManager implements
        ConnectionListener {

    private static final String TAG = RMPDApplication.APP_PREFIX + "BluetoothMPDManager";
    private RMPDApplication app = RMPDApplication.getInstance();

    private final BluetoothConnection btConnection;
    private final BluetoothMPDStatusMonitor btMonitor;
    private BluetoothMPDPlaylist playlist;

    public BluetoothMPDManager() {
        btMonitor = new BluetoothMPDStatusMonitor();
        btConnection = new BluetoothConnection(btMonitor, this);
        playlist = new BluetoothMPDPlaylist(this);
        btMonitor.addStatusChangeListener(playlist);
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
    public void connectionSucceeded(String message) {
        Log.i(TAG, "Connection succeeded in BluetoothConnection: " + message);
        app.notifyEvent(RMPDApplication.Event.CONNECTION_SUCCEEDED);
        sendCommand(new BTServerCommand(BTServerCommand.REQUEST_PLAYLIST_HASH));
    }

    @Override
    public void connectionFailed(String message) {
        Log.w(TAG, "Connection failed in BluetoothConnection: " + message);
        app.notifyEvent(RMPDApplication.Event.CONNECTION_FAILED, message);
    }

    @Override
    public void sendCommand(AbstractCommand command) {
        try {
            btConnection.sendCommand(command);
            Log.i(TAG, "Sent command: " + command);
        } catch (Exception e) {
            connectionFailed(e.getMessage());
        }
    }

    @Override
    public void addStatusChangeListener(StatusChangeListener listener) {
        btMonitor.addStatusChangeListener(listener);
    }

    @Override
    public void removeStatusChangeListener(StatusChangeListener listener) {
        btMonitor.removeStatusChangeListener(listener);
    }

    @Override
    public void addTrackPositionListener(TrackPositionListener listener) {
        btMonitor.addTrackPositionListener(listener);
    }

    @Override
    public void removeTrackPositionListener(TrackPositionListener listener) {
        btMonitor.removeTrackPositionListener(listener);
    }
}
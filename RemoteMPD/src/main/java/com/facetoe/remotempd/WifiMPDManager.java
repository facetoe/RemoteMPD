package com.facetoe.remotempd;

import android.util.Log;
import com.facetoe.remotempd.helpers.MPDAsyncHelper;
import com.facetoe.remotempd.listeners.ConnectionListener;
import org.a0z.mpd.MPD;
import org.a0z.mpd.MPDCommand;
import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.event.TrackPositionListener;
import org.a0z.mpd.exception.MPDServerException;


/**
 * Cre ated by facetoe on 2/01/14.
 */
public class WifiMPDManager extends AbstractMPDManager implements ConnectionListener {
    private final String TAG = RMPDApplication.APP_PREFIX + "WifiMPDManager";
    private final MPD mpd;
    private final RMPDApplication app = RMPDApplication.getInstance();
    private final MPDAsyncHelper asyncHelper;

    public WifiMPDManager() {
        asyncHelper = new MPDAsyncHelper();
        asyncHelper.addConnectionListener(this);
        mpd = asyncHelper.oMPD;
    }

    @Override
    public void connect() {
        Log.i(TAG, "WifiManager.connect()");

        if (!mpd.isConnected()) {
            app.notifyEvent(RMPDApplication.Event.CONNECTING);
            asyncHelper.connect();
        }

        if (!asyncHelper.isMonitorAlive())
            asyncHelper.startMonitor();
    }

    @Override
    public boolean isConnected() {
        return mpd.isConnected();
    }

    @Override
    public void disconnect() {
        asyncHelper.stopMonitor();
        asyncHelper.disconnect();
    }

    @Override
    public void sendCommand(MPDCommand command) {
        try {
            mpd.sendCommand(command);
            Log.d(TAG, "Sent command: " + command);
        } catch (MPDServerException e) {
            connectionFailed(e.getMessage());
        }
    }

    @Override
    public void connectionFailed(String message) {
        Log.w(TAG, "Connection failed in WifiMPDManager: " + message);
        app.notifyEvent(RMPDApplication.Event.CONNECTION_FAILED, message);
    }

    @Override
    public void connectionSucceeded(String message) {
        Log.i(TAG, "Connection succeeded in WifiMPDManager: " + message);
        app.notifyEvent(RMPDApplication.Event.CONNECTION_SUCCEEDED);
    }

    @Override
    public void addStatusChangeListener(StatusChangeListener listener) {
        asyncHelper.addStatusChangeListener(listener);
    }

    @Override
    public void removeStatusChangeListener(StatusChangeListener listener) {
        asyncHelper.removeStatusChangeListener(listener);
    }

    @Override
    public void addTrackPositionListener(TrackPositionListener listener) {
        asyncHelper.addTrackPositionListener(listener);
    }

    @Override
    public void removeTrackPositionListener(TrackPositionListener listener) {
        asyncHelper.removeTrackPositionListener(listener);
    }
}

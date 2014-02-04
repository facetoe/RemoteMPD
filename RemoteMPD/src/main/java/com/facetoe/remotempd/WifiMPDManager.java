package com.facetoe.remotempd;

import android.util.Log;
import com.facetoe.remotempd.helpers.MPDAsyncHelper;
import org.a0z.mpd.MPD;
import org.a0z.mpd.MPDCommand;
import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.event.TrackPositionListener;
import org.a0z.mpd.exception.MPDServerException;


/**
 * Created by facetoe on 2/01/14.
 */
public class WifiMPDManager extends AbstractMPDManager implements MPDAsyncHelper.ConnectionListener {

    String TAG = RemoteMPDApplication.APP_PREFIX + "WifiMPDManager";
    MPD mpd;
    RemoteMPDApplication app = RemoteMPDApplication.getInstance();
    MPDAsyncHelper asyncHelper;

    public WifiMPDManager() {
        asyncHelper = new MPDAsyncHelper();
        asyncHelper.addConnectionListener(this);
        mpd = asyncHelper.oMPD;
    }

    @Override
    public void connect() {
        Log.i(TAG, "WifiManager.connect()");

        if (!mpd.isConnected()) {
            asyncHelper.connect();
            app.showConnectingProgressDialog();
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
        asyncHelper.removeConnectionListener(app);
    }

    @Override
    public void sendCommand(MPDCommand command) {
        lastCommand = command;
        try {
            mpd.sendCommand(command);
            Log.i(TAG, "Sent command: " + command);
            retryAttempts = 0;
        } catch (MPDServerException e) {
            if (retryAttempts < MAX_COMMAND_RETRY_ATTEMPTS) {
                attemptReconnect();
            } else {
                handleError(e);
            }
        }
    }

    @Override
    public void connectionFailed(String message) {
        app.connectionFailed(message);
    }

    @Override
    public void connectionSucceeded(String message) {
        app.connectionSucceeded(message);
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

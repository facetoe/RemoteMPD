package com.facetoe.remotempd;

import android.util.Log;
import com.facetoe.remotempd.helpers.MPDAsyncHelper;
import org.a0z.mpd.MPD;
import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.event.TrackPositionListener;
import org.a0z.mpd.exception.MPDServerException;


/**
 * Created by facetoe on 2/01/14.
 */
public class WifiMPDManager extends AbstractMPDManager {

    String TAG = RemoteMPDApplication.APP_PREFIX + "WifiMPDManager";
    MPD mpd;
    RemoteMPDApplication app = RemoteMPDApplication.getInstance();
    MPDAsyncHelper asyncHelper;

    public WifiMPDManager() {
        asyncHelper = new MPDAsyncHelper();
        asyncHelper.addConnectionListener(app);
        mpd = asyncHelper.oMPD;
    }

    @Override
    public void connect() {
        Log.i(TAG, "WifiManager.connect()");

        if(!mpd.isConnected())
            asyncHelper.connect();

        if (!asyncHelper.isMonitorAlive())
            asyncHelper.startMonitor();
    }

    @Override
    public boolean isConnected() {
        return mpd.isConnected();
    }

    @Override
    public void restart() {
        asyncHelper.stopMonitor();
        asyncHelper.disconnect();
        asyncHelper.startMonitor();
        asyncHelper.connect();
    }

    @Override
    public void disconnect() {
        asyncHelper.stopMonitor();
        asyncHelper.disconnect();
        asyncHelper.removeConnectionListener(app);
    }

    @Override
    public void play() {
        checkState();
        try {
            mpd.play();
        } catch (MPDServerException e) {
            handleError(e);
        }
    }

    @Override
    public void playID(int id) {
        checkState();
        try {
            mpd.skipToId(id);
        } catch (MPDServerException e) {
            handleError(e);
        }
    }

    @Override
    public void stop() {
        checkState();
        try {
            mpd.stop();
        } catch (MPDServerException e) {
            handleError(e);
        }
    }

    @Override
    public void pause() {
        checkState();
        try {
            mpd.pause();
        } catch (MPDServerException e) {
            handleError(e);
        }
    }

    @Override
    public void next() {
        checkState();
        try {
            mpd.next();
        } catch (MPDServerException e) {
            handleError(e);
        }
    }

    @Override
    public void prev() {
        checkState();
        try {
            mpd.previous();
        } catch (MPDServerException e) {
            handleError(e);
        }
    }

    @Override
    public void setVolume(int newVolume) {
        checkState();
        try {
            mpd.setVolume(newVolume);
        } catch (MPDServerException e) {
            handleError(e);
        }
    }

    private void handleError(Exception ex) {
        Log.e(TAG, "Error: ", ex);
    }

    private void checkState() {
        if(!mpd.isConnected()) {
            throw new IllegalStateException("MPD is not connected!");
        }
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

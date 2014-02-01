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
public class WifiMPDManager extends AbstractMPDManager implements MPDAsyncHelper.ConnectionListener {

    String TAG = RemoteMPDApplication.APP_TAG;
    MPD mpd;
    RemoteMPDApplication app;
    MPDAsyncHelper asyncHelper;

    public WifiMPDManager() {
        asyncHelper = new MPDAsyncHelper();
        asyncHelper.addConnectionListener(this);
        mpd = asyncHelper.oMPD;
        app = RemoteMPDApplication.getInstance();
    }

    @Override
    public void connect() {
        Log.i(TAG, "WifiManager.connect()");

        if(!mpd.isConnected())
            asyncHelper.connect();

        if (!asyncHelper.isMonitorAlive())
            asyncHelper.startMonitor();

        if (app.getSongList() == null) {
            app.setSongList(asyncHelper.oMPD.getPlaylist().getMusicList());
        }
        Log.i(TAG, "Wifi connected: " + mpd.isConnected());
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
        asyncHelper.removeConnectionListener(this);
    }

    @Override
    public void play() {
        try {
            mpd.play();
        } catch (MPDServerException e) {
            handleError(e);
        }
    }

    @Override
    public void playID(int id) {
        try {
            mpd.skipToId(id);
        } catch (MPDServerException e) {
            handleError(e);
        }
    }

    @Override
    public void stop() {
        try {
            mpd.stop();
        } catch (MPDServerException e) {
            handleError(e);
        }
    }

    @Override
    public void pause() {
        try {
            mpd.pause();
        } catch (MPDServerException e) {
            handleError(e);
        }
    }

    @Override
    public void next() {
        try {
            mpd.next();
        } catch (MPDServerException e) {
            handleError(e);
        }
    }

    @Override
    public void prev() {
        try {
            mpd.previous();
        } catch (MPDServerException e) {
            handleError(e);
        }
    }

    @Override
    public void setVolume(int newVolume) {
        try {
            mpd.setVolume(newVolume);
        } catch (MPDServerException e) {
            handleError(e);
        }
    }

    @Override
    public void connectionFailed(String message) {
        Log.e(TAG, "Connection failed: " + message);
    }

    @Override
    public void connectionSucceeded(String message) {
        Log.i(TAG, "Connection succeded: " + message);
    }

    private void handleError(Exception ex) {
        Log.e(TAG, "Error: ", ex);
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
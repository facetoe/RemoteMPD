package com.facetoe.remotempd;

import android.util.Log;
import com.facetoe.remotempd.helpers.MPDAsyncHelper;
import com.facetoe.remotempd.listeners.ConnectionListener;
import org.a0z.mpd.*;
import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.event.TrackPositionListener;
import org.a0z.mpd.exception.MPDServerException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;


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
    protected void connectInternal() {
        Log.i(TAG, "WifiManager.connect()");

        if (!mpd.isConnected()) {
            asyncHelper.connect();
        }

        if (!asyncHelper.isMonitorAlive()) {
            asyncHelper.startMonitor();
        }
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
    public void sendCommand(AbstractCommand command) {
        try {
            MPDCommand mpdCommand = new MPDCommand(command.getCommand(), command.getArgs());
            mpd.getMpdConnection().sendCommand(mpdCommand);
            Log.d(TAG, "Sent command: " + command);
        } catch (MPDServerException e) {
            connectionFailed(e.getMessage());
        }
    }

    @Override
    public void play() {
        sendCommand(new MPDCommand(MPDCommand.MPD_CMD_PLAY));
    }

    @Override
    public void playID(int id) {
        sendCommand(new MPDCommand(MPDCommand.MPD_CMD_PLAY_ID, Integer.toString(id)));
    }

    @Override
    public void stop() {
        sendCommand(new MPDCommand(MPDCommand.MPD_CMD_STOP));
    }

    @Override
    public void pause() {
        sendCommand(new MPDCommand(MPDCommand.MPD_CMD_PAUSE));
    }

    @Override
    public void next() {
        sendCommand(new MPDCommand(MPDCommand.MPD_CMD_NEXT));
    }

    @Override
    public void prev() {
        sendCommand(new MPDCommand(MPDCommand.MPD_CMD_PREV));
    }

    @Override
    public void setVolume(int newVolume) {
        sendCommand(new MPDCommand(MPDCommand.MPD_CMD_VOLUME, Integer.toString(newVolume)));
    }

    @Override
    public List<String> listAlbums() {
        try {
            return mpd.listAlbums();
        } catch (MPDServerException e) {
            handleError(e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> listArtists() {
        try {
            return mpd.listArtists();
        } catch (MPDServerException e) {
            handleError(e);
        }
        return Collections.emptyList();
    }

    private void handleError(MPDServerException e) {
        connectionFailed(e.getMessage());
    }

    @Override
    public AbstractMPDPlaylist getPlaylist() {
        return mpd.getPlaylist();
    }

    @Override
    public void connectionFailed(String message) {
        Log.i(TAG, "Connection failed in WifiMPDManager: " + message);
        app.notifyEvent(RMPDApplication.Event.CONNECTION_FAILED, message);
    }

    @Override
    public void connectionSucceeded(String message) {
        Log.d(TAG, "Connection succeeded in WifiMPDManager: " + message);
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

package com.facetoe.RemoteMPD;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.facetoe.RemoteMPD.helpers.MPDAsyncHelper;
import org.a0z.mpd.MPD;
import org.a0z.mpd.MPDStatus;
import org.a0z.mpd.Music;
import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.event.TrackPositionListener;
import org.a0z.mpd.exception.MPDServerException;

import java.net.UnknownHostException;


/**
 * Created by facetoe on 2/01/14.
 */
public class WifiMPDManager extends CommandService implements MPDPlayerController {

    String TAG = RemoteMPDApplication.APP_TAG;
    MPD mpd;
    RemoteMPDApplication app;
    MPDAsyncHelper asyncHelper;

    @Override
    public void start() {
        Log.i(TAG, "WifiManager.start()");
        app = RemoteMPDApplication.getInstance();
        asyncHelper = RemoteMPDApplication.getInstance().asyncHelper;
        mpd = asyncHelper.oMPD;

        if(!asyncHelper.isMonitorAlive())
            asyncHelper.startMonitor();

        if(!mpd.isConnected())
            asyncHelper.connect();

        if(app.getSongList() == null) {
            app.setSongList(asyncHelper.oMPD.getPlaylist().getMusicList());
        }
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

    private void handleError(Exception ex) {
        Log.e(TAG, "Error: ", ex);
    }
}

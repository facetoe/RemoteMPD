package com.facetoe.RemoteMPD;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.a0z.mpd.MPDStatus;
import org.a0z.mpd.Music;
import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.event.TrackPositionListener;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by facetoe on 4/01/14.
 */
public class BluetoothMPDStatusMonitor {

    private static final String TAG = RemoteMPDApplication.APP_TAG;
    private LinkedList<StatusChangeListener> statusChangedListeners = new LinkedList<StatusChangeListener>();
    private LinkedList<TrackPositionListener> trackPositionChangedListeners = new LinkedList<TrackPositionListener>();
    Gson gson;
    public BluetoothMPDStatusMonitor() {
        gson = new Gson();
    }

    public void handleMessage(MPDResponse response) {
        String jsonString = response.getObjectJSON();
        MPDStatus status;
        switch (response.getResponseType()) {
            case MPDResponse.EVENT_UPDATE_PLAYLIST:
                extractSonglist(response);
            break;
            case MPDResponse.EVENT_TRACK:
                status = gson.fromJson(jsonString, MPDStatus.class);
                for (StatusChangeListener statusChangedListener : statusChangedListeners) {
                    statusChangedListener.trackChanged(status, -1);
                }
                break;
            default:
                Log.i(TAG, "Unknown message type: " + response.getResponseType());
                break;

        }
    }

    private void extractSonglist(MPDResponse response) {
        Type listType = new TypeToken<List<Music>>() {}.getType();
        List<Music> songList = gson.fromJson(response.getObjectJSON(), listType);
        RemoteMPDApplication.getInstance().setSongList(songList);
        Log.i(TAG, "Added " + songList.size() + " songs");
    }

    /**
     * Adds a <code>StatusChangeListener</code>.
     *
     * @param listener a <code>StatusChangeListener</code>.
     */
    public void addStatusChangeListener(StatusChangeListener listener) {
        statusChangedListeners.add(listener);
    }

    /**
     * Adds a <code>TrackPositionListener</code>.
     *
     * @param listener a <code>TrackPositionListener</code>.
     */
    public void addTrackPositionListener(TrackPositionListener listener) {
        trackPositionChangedListeners.add(listener);
    }
}

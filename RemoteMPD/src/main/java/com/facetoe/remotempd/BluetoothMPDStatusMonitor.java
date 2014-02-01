package com.facetoe.remotempd;

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

    private static final String TAG = "BluetoothMPDStatusMonitor";
    private LinkedList<StatusChangeListener> statusChangedListeners = new LinkedList<StatusChangeListener>();
    private LinkedList<TrackPositionListener> trackPositionChangedListeners = new LinkedList<TrackPositionListener>();
    Gson gson;
    public BluetoothMPDStatusMonitor() {
        gson = new Gson();
    }

    public void handleMessage(MPDResponse response) {
        String firstJSONObject = response.getObjectJSON(0);
        String secondJSONObject = null;
        if(response.getNumObjects() > 1)
            secondJSONObject = response.getObjectJSON(1);
        MPDStatus status;
        boolean random;
        boolean repeating;
        int volume;
        int oldTrack;
        int oldPlaylistVersion;
        switch (response.getResponseType()) {
            case MPDResponse.EVENT_UPDATE_PLAYLIST:
                extractSonglist(response);
            break;
            case MPDResponse.EVENT_TRACK:
                status = gson.fromJson(firstJSONObject, MPDStatus.class);
                oldTrack = gson.fromJson(secondJSONObject, int.class);
                for (StatusChangeListener listener : statusChangedListeners)
                    listener.trackChanged(status, oldTrack);
                break;
            case MPDResponse.EVENT_PLAYLIST:
                status = gson.fromJson(firstJSONObject, MPDStatus.class);
                oldPlaylistVersion = gson.fromJson(secondJSONObject, int.class);
                for (StatusChangeListener listener : statusChangedListeners)
                    listener.playlistChanged(status, oldPlaylistVersion);
                break;
            case MPDResponse.EVENT_RANDOM:
                random = gson.fromJson(firstJSONObject, boolean.class);
                for (StatusChangeListener listener : statusChangedListeners)
                    listener.randomChanged(random);
                break;
            case MPDResponse.EVENT_REPEAT:
                repeating = gson.fromJson(firstJSONObject, boolean.class);
                for (StatusChangeListener listener : statusChangedListeners)
                    listener.repeatChanged(repeating);
                break;
            case MPDResponse.EVENT_VOLUME:
                status = gson.fromJson(firstJSONObject, MPDStatus.class);
                volume = gson.fromJson(secondJSONObject, int.class);
                for (StatusChangeListener listener : statusChangedListeners) {
                    listener.volumeChanged(status, volume);
                }
                break;
            case MPDResponse.EVENT_STATE:
                status = gson.fromJson(firstJSONObject, MPDStatus.class);
                String oldState = gson.fromJson(secondJSONObject, String.class);
                for (StatusChangeListener listener : statusChangedListeners) {
                    listener.stateChanged(status, oldState);
                }
                break;
            case MPDResponse.EVENT_TRACKPOSITION:
                status = gson.fromJson(firstJSONObject, MPDStatus.class);
                for (TrackPositionListener listener : trackPositionChangedListeners) {
                    listener.trackPositionChanged(status);
                }
                break;
            default:
                Log.i(TAG, "Unknown message type: " + response.getResponseType());
                break;

        }
    }

    private void extractSonglist(MPDResponse response) {
        Type listType = new TypeToken<List<Music>>() {}.getType();
        List<Music> songList = gson.fromJson(response.getObjectJSON(0), listType);
        //RemoteMPDApplication.getInstance().setSongList(songList);
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

    public void removeStatusChangeListener(StatusChangeListener listener) {
        statusChangedListeners.remove(listener);
    }

    /**
     * Adds a <code>TrackPositionListener</code>.
     *
     * @param listener a <code>TrackPositionListener</code>.
     */
    public void addTrackPositionListener(TrackPositionListener listener) {
        trackPositionChangedListeners.add(listener);
    }

    public void removeTrackPositionListener(TrackPositionListener listener) {
        trackPositionChangedListeners.remove(listener);
    }
}

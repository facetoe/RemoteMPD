package com.facetoe.RemoteMPD;

import android.util.Log;
import com.google.gson.Gson;
import org.a0z.mpd.MPDStatus;
import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.event.TrackPositionListener;

import java.util.LinkedList;

/**
 * Created by facetoe on 4/01/14.
 */
public class BluetoothMPDStatusMonitor {

    private static final String TAG = RemoteMPDApplication.APP_TAG;
    private LinkedList<StatusChangeListener> statusChangedListeners = new LinkedList<StatusChangeListener>();
    private LinkedList<TrackPositionListener> trackPositionChangedListeners = new LinkedList<TrackPositionListener>();

    public BluetoothMPDStatusMonitor() {
    }

    public void handleMessage(MPDResponse response) {
        String jsonString = response.getObjectJSON();
        switch (response.getResponseType()) {
            case MPDResponse.PLAYER_UPDATE_CURRENTSONG:
                for (StatusChangeListener statusChangedListener : statusChangedListeners) {
                    MPDStatus status = new Gson().fromJson(jsonString, MPDStatus.class);
                    statusChangedListener.trackChanged(status, 1);
                }
                break;
            default:
                Log.i(TAG, "Unknown message type: " + response.getResponseType());
                break;

        }
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

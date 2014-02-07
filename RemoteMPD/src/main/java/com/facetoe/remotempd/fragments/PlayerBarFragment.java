package com.facetoe.remotempd.fragments;

/**
 * RemoteMPD
 * Created by facetoe on 6/02/14.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import com.facetoe.remotempd.AbstractMPDManager;
import com.facetoe.remotempd.RMPDApplication;
import com.facetoe.remotempd.listeners.MPDManagerChangeListener;
import com.facetoe.remotempd.R;
import org.a0z.mpd.MPDStatus;
import org.a0z.mpd.event.StatusChangeListener;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerBarFragment extends Fragment implements View.OnClickListener,
        MPDManagerChangeListener,
        StatusChangeListener {

    private static final String TAG = RMPDApplication.APP_PREFIX + "PlayerBarFragment";
    private ImageButton btnPlay;
    private ImageButton btnNext;
    private ImageButton btnPrev;
    private ImageButton btnShuffle;
    private ImageButton btnRepeat;

    private RMPDApplication app = RMPDApplication.getInstance();
    private AbstractMPDManager mpdManager;
    public PlayerBarFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.player_bar, container, false);
        btnPlay = (ImageButton) rootView.findViewById(R.id.btnPlay);
        btnNext = (ImageButton) rootView.findViewById(R.id.btnNext);
        btnPrev = (ImageButton) rootView.findViewById(R.id.btnPrev);
        btnRepeat = (ImageButton) rootView.findViewById(R.id.btnRepeat);
        btnShuffle = (ImageButton) rootView.findViewById(R.id.btnShuffle);

        btnPlay.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnPrev.setOnClickListener(this);
        btnRepeat.setOnClickListener(this);
        btnShuffle.setOnClickListener(this);

        app.addMpdManagerChangeListener(this);

        mpdManager = app.getMpdManager();
        mpdManager.addStatusChangeListener(this);

        // Don't kill the fragment on configuration change
        setRetainInstance(true);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        app.removeMpdManagerChangeListener(this);
        mpdManager.removeStatusChangeListener(this);
        mpdManager.disconnect();
    }

    @Override
    public void mpdManagerChanged() {
        Log.i(TAG, "MPDManagerChanged()");
        mpdManager.disconnect();
        mpdManager.removeStatusChangeListener(this);

        mpdManager = app.getMpdManager();
        mpdManager.addStatusChangeListener(this);
    }

    @Override
    public void volumeChanged(MPDStatus mpdStatus, int oldVolume) {
        Log.d(TAG, "VolumeChanged: " + oldVolume);
    }

    @Override
    public void playlistChanged(MPDStatus mpdStatus, int oldPlaylistVersion) {
        Log.d(TAG, "playlistChanged: " + oldPlaylistVersion);
    }

    @Override
    public void trackChanged(MPDStatus mpdStatus, int oldTrack) {
        Log.d(TAG, "trackChanged: " + oldTrack);
    }

    @Override
    public void stateChanged(MPDStatus mpdStatus, String oldState) {
        Log.d(TAG, "stateChanged: " + oldState);
    }

    @Override
    public void repeatChanged(boolean repeating) {
        Log.d(TAG, "repeatChanged: " + repeating);
    }

    @Override
    public void randomChanged(boolean random) {
        Log.d(TAG, "randomChanged: " + random);
    }

    @Override
    public void connectionStateChanged(boolean connected, boolean connectionLost) {
        Log.d(TAG, "connectionStateChanged: " + (connected ? "connected": "connectionLost"));
    }

    @Override
    public void libraryStateChanged(boolean updating) {
        Log.d(TAG, "libraryStateChanged - updateing: " + updating);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnPlay:
                mpdManager.play();
                break;
            case R.id.btnNext:
                mpdManager.next();
                break;
            case R.id.btnPrev:
                mpdManager.prev();
                break;
            default:
                Log.i(TAG, "Unknown: " + view.getId());
                break;
        }
    }
}
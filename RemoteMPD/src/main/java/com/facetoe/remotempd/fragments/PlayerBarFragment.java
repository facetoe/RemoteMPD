package com.facetoe.remotempd.fragments;

/**
 * RemoteMPD
 * Created by facetoe on 6/02/14.
 */

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import com.facetoe.remotempd.R;
import com.facetoe.remotempd.RMPDApplication;
import com.facetoe.remotempd.listeners.MPDManagerChangeListener;
import org.a0z.mpd.MPD;
import org.a0z.mpd.exception.MPDServerException;

/**
 * RemoteMPD
 *
 * PlayerBarFragment handles controlling the MPD player.
 */
public class PlayerBarFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = RMPDApplication.APP_PREFIX + "PlayerBarFragment";
    private final RMPDApplication app = RMPDApplication.getInstance();
    private final MPD mpd = app.getMpd();
    private ImageButton btnPlay;
    private ImageButton btnNext;
    private ImageButton btnPrev;
    private ImageButton btnShuffle;
    private ImageButton btnRepeat;

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

        // Don't kill the fragment on configuration change
        setRetainInstance(true);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnPlay:
                try {
                    mpd.play();
                } catch (MPDServerException e) {
                    handleError(e);
                }
                break;
            case R.id.btnNext:
                try {
                    mpd.next();
                } catch (MPDServerException e) {
                    handleError(e);
                }
                break;
            case R.id.btnPrev:
                try {
                    mpd.previous();
                } catch (MPDServerException e) {
                    handleError(e);
                }
                break;
            case R.id.btnShuffle:
                try {
                    Log.i(TAG, "Received albums: " + mpd.getStatistics());
                } catch (MPDServerException e) {
                    handleError(e);
                }
                break;
            case R.id.btnRepeat:
                try {
                    Log.i(TAG, "Received Dir: " + mpd.getDir());
                } catch (MPDServerException e) {
                    handleError(e);
                }
                break;
            default:
                Log.i(TAG, "Unknown: " + view.getId());
                break;
        }
    }

    private void handleError(MPDServerException e) {
        app.notifyEvent(RMPDApplication.Event.CONNECTION_FAILED, e.getMessage());
    }
}
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
import android.widget.TextView;
import com.facetoe.remotempd.R;
import com.facetoe.remotempd.RMPDApplication;
import com.facetoe.remotempd.helpers.MPDAsyncHelper;
import org.a0z.mpd.MPD;
import org.a0z.mpd.MPDStatus;
import org.a0z.mpd.Music;
import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.exception.MPDServerException;

/**
 * RemoteMPD
 * <p/>
 * PlayerBarFragment handles controlling the MPD player.
 */
public class PlayerBarFragment extends Fragment implements View.OnClickListener,
        StatusChangeListener {

    private static final String TAG = RMPDApplication.APP_PREFIX + "PlayerBarFragment";
    private final RMPDApplication app = RMPDApplication.getInstance();
    private final MPD mpd = app.getMpd();
    private final MPDAsyncHelper asyncHelper = app.getAsyncHelper();
    private ImageButton btnPlay;
    private ImageButton btnNext;
    private ImageButton btnPrev;
    private ImageButton btnRandom;
    private ImageButton btnRepeat;

    private enum PLAYER_STATE {
        PLAYING,
        PAUSED,
        STOPPED,
        UNKNOWN
    }

    PLAYER_STATE playerState = PLAYER_STATE.UNKNOWN;

    private boolean isRepeat = false;
    private boolean isRandom = false;


    private TextView txtSong;
    private TextView txtAlbum;
    private TextView txtArtist;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.player_bar, container, false);
        btnPlay = (ImageButton) rootView.findViewById(R.id.btnPlay);
        btnNext = (ImageButton) rootView.findViewById(R.id.btnNext);
        btnPrev = (ImageButton) rootView.findViewById(R.id.btnPrev);
        btnRepeat = (ImageButton) rootView.findViewById(R.id.btnRepeat);
        btnRandom = (ImageButton) rootView.findViewById(R.id.btnRandom);

        txtSong = (TextView) rootView.findViewById(R.id.txtSong);
        txtAlbum = (TextView) rootView.findViewById(R.id.txtAlbum);
        txtArtist = (TextView) rootView.findViewById(R.id.txtArtist);

        btnPlay.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnPrev.setOnClickListener(this);
        btnRepeat.setOnClickListener(this);
        btnRandom.setOnClickListener(this);

        // Don't kill the fragment on configuration change
        setRetainInstance(true);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onStop() {
        super.onStop();
        asyncHelper.removeStatusChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        asyncHelper.addStatusChangeListener(this);
        checkButtonPlayImage();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkButtonPlayImage();
    }

    private void checkButtonPlayImage() {
        if (mpd.isConnected()) {
            try {
                String currentState = mpd.getStatus().getState();
                setState(currentState);
                setButtonPlayImage();
            } catch (MPDServerException e) {
                handleError(e);
            }
        }
    }

    private void setState(String newState) {
        String logMessage = "State changed from " + playerState + " to ";
        if (newState.equals(MPDStatus.MPD_STATE_PLAYING)) {
            playerState = PLAYER_STATE.PLAYING;
        } else if (newState.equals(MPDStatus.MPD_STATE_PAUSED)) {
            playerState = PLAYER_STATE.PAUSED;
        } else if (newState.equals(MPDStatus.MPD_STATE_STOPPED)) {
            playerState = PLAYER_STATE.STOPPED;
        } else if (newState.equals(MPDStatus.MPD_STATE_UNKNOWN)) {
            playerState = PLAYER_STATE.UNKNOWN;
        } else {
            Log.w(TAG, "Invalid state passed to setState()");
        }
        Log.d(TAG, logMessage + playerState);
    }

    private void setButtonPlayImage() {
        if (playerState == PLAYER_STATE.PLAYING) {
            btnPlay.setImageResource(R.drawable.ic_media_pause);
        } else if (playerState == PLAYER_STATE.PAUSED || playerState == PLAYER_STATE.STOPPED) {
            btnPlay.setImageResource(R.drawable.ic_media_play);
        } else {
            Log.w(TAG, "Unknown state: " + playerState);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnPlay:
                handleBtnPlay();
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
            case R.id.btnRandom:
                handleBtnRandom();
                break;
            case R.id.btnRepeat:
                handleBtnRepeat();
                break;
            default:
                Log.i(TAG, "Unknown: " + view.getId());
                break;
        }
    }

    private void handleBtnRepeat() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(isRepeat) {
                        isRepeat = false;
                        mpd.setRepeat(isRepeat);
                    } else {
                        isRepeat = true;
                        mpd.setRepeat(isRepeat);
                    }
                } catch (MPDServerException e) {
                    handleError(e);
                }
            }
        }).start();
    }

    private void handleBtnRandom() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(isRandom) {
                        isRandom = false;
                        mpd.setRandom(isRandom);
                    } else {
                        isRandom = true;
                        mpd.setRandom(isRandom);
                    }
                } catch (MPDServerException e) {
                    handleError(e);
                }
            }
        }).start();
    }

    private void handleBtnPlay() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (playerState == PLAYER_STATE.PLAYING) {
                        mpd.stop();
                    } else if (playerState == PLAYER_STATE.STOPPED || playerState == PLAYER_STATE.PAUSED) {
                        mpd.play();
                    }
                } catch (MPDServerException e) {
                    app.notifyEvent(RMPDApplication.Event.CONNECTION_FAILED, e.getMessage());
                }
            }
        }).start();
    }

    @Override
    public void volumeChanged(MPDStatus mpdStatus, int oldVolume) {
        Log.i(TAG, "Volume Changed");
    }

    @Override
    public void playlistChanged(MPDStatus mpdStatus, int oldPlaylistVersion) {
        Log.i(TAG, "Playlist changed");
        Music currentSong = getCurrentSong(mpdStatus);
        updatePlayerBarText(currentSong);
    }

    @Override
    public void trackChanged(MPDStatus mpdStatus, int oldTrack) {
        Music currentSong = getCurrentSong(mpdStatus);
        Log.i(TAG, "Track changed to: " + currentSong);
        updatePlayerBarText(currentSong);
    }

    private Music getCurrentSong(MPDStatus status) {
        return mpd.getPlaylist().getByIndex(status.getSongPos());
    }

    private void updatePlayerBarText(Music currentSong) {
        if (currentSong != null) {
            txtSong.setText(currentSong.getTitle());
            txtAlbum.setText(currentSong.getAlbum());
            txtArtist.setText(currentSong.getArtist());
        }
    }

    @Override
    public void stateChanged(MPDStatus mpdStatus, String oldState) {
        setState(mpdStatus.getState());
        setButtonPlayImage();
    }

    @Override
    public void repeatChanged(boolean repeating) {
        Log.i(TAG, "Repeat Changed");
    }

    @Override
    public void randomChanged(boolean random) {
        Log.i(TAG, "Random changed");
    }

    @Override
    public void connectionStateChanged(boolean connected, boolean connectionLost) {
        Log.i(TAG, "Connection state changed");
    }

    @Override
    public void libraryStateChanged(boolean updating) {
        Log.i(TAG, "Library state changed");
    }

    private void handleError(MPDServerException e) {
        app.notifyEvent(RMPDApplication.Event.CONNECTION_FAILED, e.getMessage());
    }
}
package com.facetoe.remotempd.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
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
 * PlayerControlFragment handles controlling the MPD player.
 */
public class PlayerControlFragment extends Fragment implements View.OnClickListener,
        StatusChangeListener {

    private static final String TAG = RMPDApplication.APP_PREFIX + "PlayerControlFragment";
    private final RMPDApplication app = RMPDApplication.getInstance();
    private final MPD mpd = app.getMpd();
    private final MPDAsyncHelper asyncHelper = app.getAsyncHelper();
    private ImageButton btnPlay;
    private ImageButton btnNext;
    private ImageButton btnPrev;
    private ImageButton btnRandom;
    private ImageButton btnRepeat;

    private TextView txtSong;
    private TextView txtAlbumArtist;
    private SeekBar seekTrack;
    private SeekBar seekVolume;

    private LinearLayout nowPlayingLayout;

    private enum PLAYER_STATE {
        PLAYING,
        PAUSED,
        STOPPED,
        UNKNOWN
    }

    private PLAYER_STATE playerState = PLAYER_STATE.UNKNOWN;
    private MPDStatus mpdStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.player_bar, container, false);
        assert rootView != null;
        btnPlay = (ImageButton) rootView.findViewById(R.id.btnPlay);
        btnNext = (ImageButton) rootView.findViewById(R.id.btnNext);
        btnPrev = (ImageButton) rootView.findViewById(R.id.btnPrev);
        btnRepeat = (ImageButton) rootView.findViewById(R.id.btnRepeat);
        btnRandom = (ImageButton) rootView.findViewById(R.id.btnRandom);

        txtSong = (TextView)rootView.findViewById(R.id.txtSong);
        txtAlbumArtist = (TextView)rootView.findViewById(R.id.txtAritstAlbum);

        seekTrack = (SeekBar)rootView.findViewById(R.id.seekTrackProgress);
        seekVolume = (SeekBar)rootView.findViewById(R.id.seekVolume);

        nowPlayingLayout = (LinearLayout)rootView.findViewById(R.id.nowPlayingLayout);

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
    public void onStop() {
        super.onStop();
        asyncHelper.removeStatusChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        asyncHelper.addStatusChangeListener(this);
        if (mpd.isConnected()) {
            initializePlayerBar();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        asyncHelper.removeStatusChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mpd.isConnected()) {
            initializePlayerBar();
        }
    }

    private void initializePlayerBar() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mpdStatus = mpd.getStatus();
                    String state = mpdStatus.getState();
                    setState(state);
                    setButtonPlayIcon();
                    updateRepeatRandomButtons(mpdStatus);
                    updateNowPlayingText(mpdStatus);

                } catch (MPDServerException e) {
                    handleError(e);
                }
            }
        }).start();
    }

    private void updateRepeatRandomButtons(final MPDStatus status) {
        Activity parentActivity = getActivity();
        if (parentActivity != null) {
            parentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setRepeatButtonIcon(status.isRepeat());
                    setRandomButtonIcon(status.isRandom());
                }
            });
        }
    }

    private void setButtonPlayIcon() {
        Activity parentActivity = getActivity();
        if (parentActivity != null) {
            parentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (playerState == PLAYER_STATE.PLAYING) {
                        btnPlay.setImageResource(R.drawable.ic_media_pause);
                    } else if (playerState == PLAYER_STATE.PAUSED || playerState == PLAYER_STATE.STOPPED) {
                        btnPlay.setImageResource(R.drawable.ic_media_play);
                    } else {
                        Log.w(TAG, "Unknown state: " + playerState);
                    }
                }
            });
        }
    }

    private void updateNowPlayingText(MPDStatus status) {
        final Music currentSong = getCurrentSong(status);
        Activity parentActivity = getActivity();
        if (parentActivity != null) {
            parentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    // If currentSong is null then the playlist is empty, so nothing is "now playing".
                    if (currentSong == null) {
                        Log.i(TAG, "Current song was null");
                        nowPlayingLayout.setVisibility(View.GONE);
                    } else {
                        Log.i(TAG, "Updating text");
                        nowPlayingLayout.setVisibility(View.VISIBLE);
                        txtSong.setText(currentSong.getTitle());
                        txtAlbumArtist.setText(currentSong.getArtist() + " - " + currentSong.getAlbum());
                    }
                }
            });
        }

    }

    private Music getCurrentSong(MPDStatus status) {
        return mpd.getPlaylist().getByIndex(status.getSongPos());
    }

    private void setState(String newState) {
        if (newState.equals(playerState.toString())) {
            return;
        }
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnPlay:
                handleBtnPlay();
                break;
            case R.id.btnNext:
                handleBtnNext();
                break;
            case R.id.btnPrev:
                handleBtnPrevious();
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

    private void handleBtnPlay() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (playerState == PLAYER_STATE.PLAYING) {
                        mpd.pause();
                    } else if (playerState == PLAYER_STATE.STOPPED || playerState == PLAYER_STATE.PAUSED) {
                        mpd.play();
                    }
                } catch (MPDServerException e) {
                    handleError(e);
                }
            }
        }).start();
    }

    private void handleBtnNext() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mpd.next();
                } catch (MPDServerException e) {
                    handleError(e);
                }
            }
        }).start();
    }

    private void handleBtnPrevious() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mpd.previous();
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
                    mpd.setRandom(!mpdStatus.isRandom());
                } catch (MPDServerException e) {
                    handleError(e);
                }
            }
        }).start();
    }

    private void handleBtnRepeat() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mpd.setRepeat(!mpdStatus.isRepeat());
                } catch (MPDServerException e) {
                    handleError(e);
                }
            }
        }).start();
    }

    @Override
    public void volumeChanged(MPDStatus mpdStatus, int oldVolume) {
        this.mpdStatus = mpdStatus;
        Log.d(TAG, "Volume Changed");
    }

    @Override
    public void playlistChanged(MPDStatus mpdStatus, int oldPlaylistVersion) {
        this.mpdStatus = mpdStatus;
        updateNowPlayingText(mpdStatus);
        Log.d(TAG, "Playlist changed");
    }

    @Override
    public void trackChanged(MPDStatus mpdStatus, int oldTrack) {
        this.mpdStatus = mpdStatus;
        Music currentSong = getCurrentSong(mpdStatus);
        Log.d(TAG, "Track changed to: " + currentSong);
        updateNowPlayingText(mpdStatus);
    }

    @Override
    public void stateChanged(MPDStatus mpdStatus, String oldState) {
        this.mpdStatus = mpdStatus;
        setState(mpdStatus.getState());
        setButtonPlayIcon();
    }

    @Override
    public void repeatChanged(boolean repeating) {
        Log.d(TAG, "Repeat Changed: " + repeating);
        setRepeatButtonIcon(repeating);
    }

    private void setRepeatButtonIcon(boolean repeating) {
        if(repeating) {
            btnRepeat.setImageResource(R.drawable.ic_media_repeat_on);
        } else {
            btnRepeat.setImageResource(R.drawable.ic_media_repeat);
        }
    }

    @Override
    public void randomChanged(boolean random) {
        Log.d(TAG, "Random changed: " + random);
        setRandomButtonIcon(random);
    }

    private void setRandomButtonIcon(boolean random) {
        if(random) {
            btnRandom.setImageResource(R.drawable.ic_media_shuffle_on);
        } else {
            btnRandom.setImageResource(R.drawable.ic_media_shuffle);
        }
    }

    @Override
    public void connectionStateChanged(boolean connected, boolean connectionLost) {
        Log.i(TAG, "Connection state changed: " + connected);
    }

    @Override
    public void libraryStateChanged(boolean updating) {
        Log.d(TAG, "Library state changed: " + updating);
    }

    private void handleError(MPDServerException e) {
        app.notifyEvent(RMPDApplication.Event.CONNECTION_FAILED, e.getMessage());
    }
}
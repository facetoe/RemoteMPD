package com.facetoe.remotempd.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.facetoe.remotempd.R;
import com.facetoe.remotempd.RMPDApplication;
import com.facetoe.remotempd.helpers.MPDAsyncHelper;
import org.a0z.mpd.MPD;
import org.a0z.mpd.MPDStatus;
import org.a0z.mpd.Music;
import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.event.TrackPositionListener;
import org.a0z.mpd.exception.MPDServerException;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * RemoteMPD
 * PlayerControlFragment handles controlling the MPD player.
 */
public class PlayerControlFragment extends Fragment implements View.OnClickListener,
        StatusChangeListener, TrackPositionListener {

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
    private TextView txtAlbum;
    private TextView txtArtist;
    private TextView totalTrackTime;
    private TextView currentTrackTime;

    private SeekBar seekTrack;
    private SeekBar seekVolume;

    private LinearLayout nowPlayingLayout;
    private Handler handler;

    private enum State {
        PLAYING,
        PAUSED,
        STOPPED,
        UNKNOWN
    }

    private State currentState = State.UNKNOWN;
    private MPDStatus mpdStatus;

    private Timer trackPositionTimer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
    }

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

        txtSong = (TextView) rootView.findViewById(R.id.txtSong);
        txtAlbum = (TextView) rootView.findViewById(R.id.txtAlbum);
        txtArtist = (TextView) rootView.findViewById(R.id.txtArtist);
        totalTrackTime = (TextView) rootView.findViewById(R.id.totalTrackTime);
        currentTrackTime = (TextView) rootView.findViewById(R.id.currentTrackTime);

        seekVolume = (SeekBar) rootView.findViewById(R.id.seekVolume);
        seekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.i(TAG, "Volume set to: " + seekVolume.getProgress());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mpd.setVolume(seekVolume.getProgress());
                        } catch (MPDServerException e) {
                            handleError(e);
                        }
                    }
                }).start();
            }
        });

        seekTrack = (SeekBar) rootView.findViewById(R.id.seekProgress);
        seekTrack.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(getCurrentSong(mpdStatus) != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mpd.seek((long) seekTrack.getProgress());
                            } catch (MPDServerException e) {
                                handleError(e);
                            }
                        }
                    }).start();
                }
            }
        });

        nowPlayingLayout = (LinearLayout) rootView.findViewById(R.id.nowPlayingLayout);

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
        asyncHelper.addTrackPositionListener(this);
        if (mpd.isConnected()) {
            initializePlayerBar();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        asyncHelper.removeStatusChangeListener(this);
        asyncHelper.removeTrackPositionListener(this);
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
                    setCurrentState(state);
                    setButtonPlayIcon();
                    updateRepeatRandomButtons(mpdStatus);
                    updateNowPlayingText(mpdStatus);
                    maybeStartTrackPositionTimer();

                } catch (MPDServerException e) {
                    handleError(e);
                }
            }
        }).start();
    }

    private void setVolumeAndTrackPosition() {
        seekVolume.setProgress(mpdStatus.getVolume());
        seekTrack.setMax((int) mpdStatus.getTotalTime());
        seekTrack.setProgress((int) mpdStatus.getElapsedTime());
    }


    private void updateRepeatRandomButtons(final MPDStatus status) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                setRepeatButtonIcon(status.isRepeat());
                setRandomButtonIcon(status.isRandom());
            }
        });
    }

    private void setButtonPlayIcon() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (currentState == State.PLAYING) {
                    btnPlay.setImageResource(R.drawable.ic_media_pause);
                } else if (currentState == State.PAUSED || currentState == State.STOPPED) {
                    btnPlay.setImageResource(R.drawable.ic_media_play);
                } else {
                    Log.w(TAG, "Unknown state: " + currentState);
                }
            }
        });
    }

    private void updateNowPlayingText(final MPDStatus status) {
        final Music currentSong = getCurrentSong(status);
        handler.post(new Runnable() {
            @Override
            public void run() {
                // If currentSong is null then the playlist is empty.
                if (currentSong == null || mpd.getPlaylist().size() == 0) {
                    nowPlayingLayout.setVisibility(View.GONE);
                    seekTrack.setEnabled(false);
                    seekTrack.setProgress(0);
                    currentTrackTime.setText("00:00");
                    totalTrackTime.setText("00:00");
                } else {
                    nowPlayingLayout.setVisibility(View.VISIBLE);
                    seekTrack.setEnabled(true);
                    txtSong.setText(currentSong.getTitle());
                    txtAlbum.setText(currentSong.getAlbum());
                    txtArtist.setText(currentSong.getArtist());
                    totalTrackTime.setText(timeToString(status.getTotalTime()));
                    setVolumeAndTrackPosition();
                }
            }
        });
    }

    private Music getCurrentSong(MPDStatus status) {
        return mpd.getPlaylist().getByIndex(status.getSongPos());
    }

    private void setCurrentState(String newState) {
        if (newState.equals(currentState.toString())) {
            return;
        }
        String logMessage = "State changed from " + currentState + " to ";
        if (newState.equals(MPDStatus.MPD_STATE_PLAYING)) {
            currentState = State.PLAYING;
        } else if (newState.equals(MPDStatus.MPD_STATE_PAUSED)) {
            currentState = State.PAUSED;
        } else if (newState.equals(MPDStatus.MPD_STATE_STOPPED)) {
            currentState = State.STOPPED;
        } else if (newState.equals(MPDStatus.MPD_STATE_UNKNOWN)) {
            currentState = State.UNKNOWN;
        } else {
            Log.w(TAG, "Invalid state passed to setState()");
        }
        Log.d(TAG, logMessage + currentState);
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
                    if (currentState == State.PLAYING) {
                        mpd.pause();
                    } else if (currentState == State.STOPPED || currentState == State.PAUSED) {
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
        seekVolume.setProgress(mpdStatus.getVolume());
    }

    @Override
    public void playlistChanged(MPDStatus mpdStatus, int oldPlaylistVersion) {
        Log.i(TAG, "Playlist changed");
        this.mpdStatus = mpdStatus;
        updateNowPlayingText(mpdStatus);
    }

    @Override
    public void trackChanged(MPDStatus mpdStatus, int oldTrack) {
        Log.i(TAG, "Track changed");
        this.mpdStatus = mpdStatus;
        updateNowPlayingText(mpdStatus);
        maybeStartTrackPositionTimer();
    }

    @Override
    public void trackPositionChanged(MPDStatus status) {
        this.mpdStatus = status;
        maybeStartTrackPositionTimer();
    }

    private void maybeStartTrackPositionTimer() {
        if (currentState == State.PLAYING) {
            startTrackPositionTimer();
        } else if(currentState == State.PAUSED || currentState == State.STOPPED) {
            stopTrackPositionTimer();
        }
    }

    private void startTrackPositionTimer() {
        stopTrackPositionTimer();
        trackPositionTimer = new Timer();
        TrackPositionTimerTask trackPositionTimerTask = new TrackPositionTimerTask(mpdStatus.getElapsedTime());
        trackPositionTimer.scheduleAtFixedRate(trackPositionTimerTask, 0, 1000);
    }

    private void stopTrackPositionTimer() {
        if (null != trackPositionTimer) {
            trackPositionTimer.cancel();
            trackPositionTimer = null;
        }
    }

    private class TrackPositionTimerTask extends TimerTask {
        Date date = new Date();
        long start = 0;
        long elapsed = 0;

        public TrackPositionTimerTask(long start) {
            this.start = start;
        }

        @Override
        public void run() {
            Date now = new Date();
            elapsed = start + ((now.getTime() - date.getTime()) / 1000);
            if(elapsed >= mpdStatus.getTotalTime()) {
                Log.i(TAG, "Song is finished, canceling task");
                cancel();
                return;
            }

            seekTrack.setProgress((int) elapsed);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    currentTrackTime.setText(timeToString(elapsed));
                }
            });
        }
    }

    private static String timeToString(long seconds) {
        if (seconds < 0) {
            seconds = 0;
        }

        long hours = seconds / 3600;
        seconds -= 3600 * hours;
        long minutes = seconds / 60;
        seconds -= minutes * 60;
        if (hours == 0) {
            return String.format("%02d:%02d", minutes, seconds);
        } else {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
    }

    @Override
    public void stateChanged(MPDStatus mpdStatus, String oldState) {
        this.mpdStatus = mpdStatus;
        setCurrentState(mpdStatus.getState());
        setButtonPlayIcon();
        maybeStartTrackPositionTimer();
    }

    @Override
    public void repeatChanged(boolean repeating) {
        Log.d(TAG, "Repeat Changed: " + repeating);
        setRepeatButtonIcon(repeating);
    }

    private void setRepeatButtonIcon(boolean repeating) {
        if (repeating) {
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
        if (random) {
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
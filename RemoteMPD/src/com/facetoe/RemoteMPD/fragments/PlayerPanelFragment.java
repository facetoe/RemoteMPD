package com.facetoe.RemoteMPD.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.facetoe.RemoteMPD.AbstractMPDManager;
import com.facetoe.RemoteMPD.R;
import com.facetoe.RemoteMPD.RemoteMPDApplication;
import com.facetoe.RemoteMPD.adapters.SongListAdapter;
import org.a0z.mpd.MPDStatus;
import org.a0z.mpd.Music;
import org.a0z.mpd.event.StatusChangeListener;

import java.util.List;

/**
 * Created by facetoe on 5/01/14.
 */
public class PlayerPanelFragment extends Fragment implements View.OnClickListener, StatusChangeListener {
    String TAG = RemoteMPDApplication.APP_TAG;

    private ImageButton btnNext;
    private ImageButton btnPrev;
    private ImageButton btnPlay;
    private TextView txtCurrentSong;
    private TextView txtCurrentAlbum;
    private ListView songListView;
    private SongListAdapter songListAdapter;
    private List<Music> songList;
    RemoteMPDApplication app = RemoteMPDApplication.getInstance();
    AbstractMPDManager mpdManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app.setCurrentActivity(getActivity());
        Log.i(TAG, "OnCreate()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "OnCreateView()");
        View view = inflater.inflate(R.layout.player_panel_fragment, container, false);
        txtCurrentAlbum = (TextView) view.findViewById(R.id.txtCurrentAlbum);
        txtCurrentSong = (TextView) view.findViewById(R.id.txtCurrentSong);
        mpdManager = app.getMpdManager();
        mpdManager.connect();
        mpdManager.addStatusChangeListener(this);
        btnNext = (ImageButton) view.findViewById(R.id.btnNext);
        btnNext.setOnClickListener(this);
        btnPrev = (ImageButton) view.findViewById(R.id.btnBack);
        btnPrev.setOnClickListener(this);
        btnPlay = (ImageButton) view.findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(this);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mpdManager.removeStatusChangeListener(this);
    }

    @Override
    public void volumeChanged(MPDStatus mpdStatus, int oldVolume) {
        Log.i(TAG, "Volume changed: " + mpdStatus.getVolume());
    }

    @Override
    public void playlistChanged(MPDStatus mpdStatus, int oldPlaylistVersion) {
        Log.i(TAG, "Playlist changed");
    }

    @Override
    public void trackChanged(MPDStatus mpdStatus, int oldTrack) {
        Log.i(TAG, "trackChanged()");
        songList = app.getSongList();
        if (songList != null) {
            int pos = mpdStatus.getSongPos();
            final Music song = songList.get(pos);
            Activity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtCurrentSong.setText(song.getTitle());
                        txtCurrentAlbum.setText(song.getAlbum());
                    }
                });
            }
        } else {
            Log.e(TAG, "Song list was null");
        }
    }

    @Override
    public void stateChanged(MPDStatus mpdStatus, String oldState) {
        Log.i(TAG, "State changed");
    }

    @Override
    public void repeatChanged(boolean repeating) {
        Log.i(TAG, "Repeat changed");

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnNext:
                mpdManager.next();
                break;
            case R.id.btnBack:
                mpdManager.prev();
                break;
            case R.id.btnPlay:
                mpdManager.play();
                break;
            default:
                return;
        }
    }
}

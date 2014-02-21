package com.facetoe.remotempd.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.facetoe.remotempd.R;
import com.facetoe.remotempd.RMPDApplication;
import com.facetoe.remotempd.adapters.MusicAdapter;
import org.a0z.mpd.MPDPlaylist;
import org.a0z.mpd.MPDStatus;
import org.a0z.mpd.Music;
import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.exception.MPDServerException;


/**
 * RemoteMPD
 * Created by facetoe on 11/02/14.
 */
public class PlaylistFragment extends Fragment implements AdapterView.OnItemClickListener, StatusChangeListener {
    RMPDApplication app = RMPDApplication.getInstance();
    MPDPlaylist playlist = app.getMpd().getPlaylist();
    ListView listPlaylist;
    MusicAdapter musicAdapter;

    private String TAG = RMPDApplication.APP_PREFIX + "PlaylistFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.playlist, container, false);
        listPlaylist = (ListView) rootView.findViewById(R.id.listPlaylist);
        listPlaylist.setFastScrollEnabled(true);
        listPlaylist.setFastScrollAlwaysVisible(true);
        listPlaylist.setOnItemClickListener(this);
        app.getAsyncHelper().addStatusChangeListener(this);

        musicAdapter = new MusicAdapter(getActivity(), playlist.getMusicList());
        listPlaylist.setAdapter(musicAdapter);

        EditText txtSearch = (EditText)rootView.findViewById(R.id.txtSearch);
        txtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                musicAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void volumeChanged(MPDStatus mpdStatus, int oldVolume) {

    }

    @Override
    public void playlistChanged(MPDStatus mpdStatus, int oldPlaylistVersion) {
        Activity parentActivity = getActivity();
        if (parentActivity != null) {
            parentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Updated playlist with " + playlist.size() + " songs");
                    musicAdapter.updatePlaylist(app.getMpd().getPlaylist().getMusicList());
                }
            });
        }
    }

    @Override
    public void trackChanged(MPDStatus mpdStatus, int oldTrack) {

    }

    @Override
    public void stateChanged(MPDStatus mpdStatus, String oldState) {

    }

    @Override
    public void repeatChanged(boolean repeating) {

    }

    @Override
    public void randomChanged(boolean random) {

    }

    @Override
    public void connectionStateChanged(boolean connected, boolean connectionLost) {

    }

    @Override
    public void libraryStateChanged(boolean updating) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Music song = musicAdapter.getItem(position);
        try {
            RMPDApplication.getInstance().getMpd().skipToId(song.getSongId());
        } catch (MPDServerException e) {
            app.notifyEvent(RMPDApplication.Event.CONNECTION_FAILED, e.getMessage());
        }
    }
}

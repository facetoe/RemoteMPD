package com.facetoe.remotempd.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
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
import org.a0z.mpd.Music;
import org.a0z.mpd.exception.MPDServerException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



/**
 * RemoteMPD
 * Created by facetoe on 11/02/14.
 */
public class PlaylistFragment extends Fragment {
//    ListView listPlaylist;
//    AbstractMPDPlaylist playlist;
//    MusicAdapter musicAdapter;
//    private String TAG = RMPDApplication.APP_PREFIX + "PlaylistFragment";
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View rootView = inflater.inflate(R.layout.playlist, container, false);
//        listPlaylist = (ListView) rootView.findViewById(R.id.listPlaylist);
//        listPlaylist.setFastScrollEnabled(true);
//        listPlaylist.setFastScrollAlwaysVisible(true);
//
//        EditText txtSearch = (EditText)rootView.findViewById(R.id.txtSearch);
//        txtSearch.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                musicAdapter.getFilter().filter(s);
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
//
//        app.addMpdManagerChangeListener(this);
//        setPlaylistUpdateListener();
//
//        return rootView;
//    }
//
//    private void setPlaylistUpdateListener() {
//        Log.i(TAG, "setPlaylistUpdateListener called in fragment");
//        playlist = mpdManager.getPlaylist();
//        musicAdapter = new MusicAdapter(getActivity(), playlist.getMusicList());
//        listPlaylist.setAdapter(musicAdapter);
//        playlist.setPlaylistUpdateListener(this);
//        listPlaylist.setOnItemClickListener(this);
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        app.removeMpdManagerChangeListener(this);
//    }
//
//    @Override
//    public void playlistUpdated() {
//        Log.i(TAG, "playlistUpdated()");
//        Activity parentActivity = getActivity();
//        if (parentActivity != null) {
//            parentActivity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    musicAdapter.notifyDataSetChanged();
//                }
//            });
//        }
//    }
//
//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        Music song = musicAdapter.getItem(position);
//        Log.i(TAG, "Called");
//        try {
//            RMPDApplication.getInstance().getMpd().skipToId(song.getSongId());
//        } catch (MPDServerException e) {
//            Log.e(TAG, "Didn't work", e);
//        }
//    }
//
//    @Override
//    public void mpdManagerChanged() {
//        super.mpdManagerChanged();
//        setPlaylistUpdateListener();
//    }
}

package com.facetoe.remotempd.fragments;

import android.app.Activity;
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
import org.a0z.mpd.AbstractMPDPlaylist;
import org.a0z.mpd.Music;
import org.a0z.mpd.exception.MPDServerException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class MusicAdapter extends ArrayAdapter<Music> {
    private final Context context;
    private List<Music> items;
    private final List<Music> storedItems;
    private static final String TAG = RMPDApplication.APP_PREFIX + "MusicAdapter";

    public MusicAdapter(Context context, List<Music> items) {
        super(context, R.layout.song_list, items);
        this.context = context;
        this.items = items;
        storedItems = new ArrayList<Music>(items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(R.layout.song_list, parent, false);
        TextView name = (TextView) rowView.findViewById(R.id.name);
        TextView songLength = (TextView) rowView.findViewById(R.id.songLength);
        Music song;

        song = items.get(position);

        name.setText(song.getTitle());
        songLength.setText(song.getFormatedTime());

        return rowView;
    }

    @Override
    public Music getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                if (constraint == null || constraint.length() == 0) {
                    // No filter implemented we return all the list
                    results.values = storedItems;
                    results.count = storedItems.size();
                }
                else {
                    List<Music> musicMatches = new ArrayList<Music>();

                    for (Music music : storedItems) {
                        if (music.getTitle().toUpperCase().startsWith(constraint.toString().toUpperCase()))
                            musicMatches.add(music);
                    }

                    results.values = musicMatches;
                    results.count = musicMatches.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.count == 0) {
                    items = Collections.emptyList();
                    notifyDataSetInvalidated();
                } else {
                    items = (List<Music>) results.values;
                    notifyDataSetChanged();
                    Log.v(TAG, "Updating with " + results.count + " results");
                }
            }
        };
    }
}

/**
 * RemoteMPD
 * Created by facetoe on 11/02/14.
 */
public class PlaylistFragment extends AbstractRMPDFragment
        implements AbstractMPDPlaylist.PlaylistUpdateListener,
        AdapterView.OnItemClickListener {
    ListView listPlaylist;
    AbstractMPDPlaylist playlist;
    MusicAdapter musicAdapter;
    private String TAG = RMPDApplication.APP_PREFIX + "PlaylistFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.playlist, container, false);
        listPlaylist = (ListView) rootView.findViewById(R.id.listPlaylist);
        listPlaylist.setFastScrollEnabled(true);
        listPlaylist.setFastScrollAlwaysVisible(true);

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

        app.addMpdManagerChangeListener(this);
        setPlaylistUpdateListener();

        return rootView;
    }

    private void setPlaylistUpdateListener() {
        Log.i(TAG, "setPlaylistUpdateListener called in fragment");
        playlist = mpdManager.getPlaylist();
        musicAdapter = new MusicAdapter(getActivity(), playlist.getMusicList());
        listPlaylist.setAdapter(musicAdapter);
        playlist.setPlaylistUpdateListener(this);
        listPlaylist.setOnItemClickListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        app.removeMpdManagerChangeListener(this);
    }

    @Override
    public void playlistUpdated() {
        Log.i(TAG, "playlistUpdated()");
        Activity parentActivity = getActivity();
        if (parentActivity != null) {
            parentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    musicAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Music song = musicAdapter.getItem(position);
        Log.i(TAG, "Called");
        try {
            RMPDApplication.getInstance().getMpdManager().skipToId(song.getSongId());
        } catch (MPDServerException e) {
            Log.e(TAG, "Didn't work", e);
        }
    }

    @Override
    public void mpdManagerChanged() {
        super.mpdManagerChanged();
        setPlaylistUpdateListener();
    }
}

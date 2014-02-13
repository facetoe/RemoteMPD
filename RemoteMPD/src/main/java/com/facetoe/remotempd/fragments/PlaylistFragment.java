package com.facetoe.remotempd.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.facetoe.remotempd.R;
import com.facetoe.remotempd.RMPDApplication;
import org.a0z.mpd.AbstractMPDPlaylist;
import org.a0z.mpd.Music;

import java.util.List;

class MusicAdapter extends ArrayAdapter<Music> {
    private final Context context;
    private final List<Music> values;

    public MusicAdapter(Context context, List<Music> values) {
        super(context, R.layout.song_list, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.song_list, parent, false);
        TextView name = (TextView) rowView.findViewById(R.id.name);
        TextView artist = (TextView) rowView.findViewById(R.id.artist);
        TextView songLength = (TextView) rowView.findViewById(R.id.songLength);

        Music song = values.get(position);
        if (song != null) {

            name.setText(song.getName());
            artist.setText(song.getArtist());
            songLength.setText(String.valueOf(song.getTime()));
        } else {
            name.setText("null");
        }
        return rowView;
    }
}

/**
 * RemoteMPD
 * Created by facetoe on 11/02/14.
 */
public class PlaylistFragment extends AbstractRMPDFragment implements AbstractMPDPlaylist.PlaylistUpdateListener {
    ListView listPlaylist;
    AbstractMPDPlaylist playlist;
    MusicAdapter musicAdapter;
    private String TAG = RMPDApplication.APP_PREFIX + "PlaylistFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.playlist, container, false);
        listPlaylist = (ListView) rootView.findViewById(R.id.listPlaylist);
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
    public void mpdManagerChanged() {
        super.mpdManagerChanged();
        setPlaylistUpdateListener();
    }
}

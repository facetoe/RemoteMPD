package com.facetoe.remotempd.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.facetoe.remotempd.R;
import org.a0z.mpd.Item;
import org.a0z.mpd.Music;

import java.util.List;

/**
 * RemoteMPD
 * Created by facetoe on 4/03/14.
 */
public class PlaylistAdapter extends AbstractMPDArrayAdapter {
    public PlaylistAdapter(Context context, List<Item> items) {
        super(context, R.layout.playlist_item, items);
    }

    int nowPlayingID = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(R.layout.playlist_item, parent, false);
        TextView songName = (TextView) rowView.findViewById(R.id.songName);
        TextView artistAlbum = (TextView) rowView.findViewById(R.id.artistAlbum);

        Music playlistSong = (Music) getItem(position);

        ImageView playIcon = (ImageView)rowView.findViewById(R.id.playingIcon);
        if(nowPlayingID == playlistSong.getSongId()) {
            playIcon.setVisibility(View.VISIBLE);
        } else {
            playIcon.setVisibility(View.INVISIBLE);
        }

        songName.setText(playlistSong.getTitle());
        artistAlbum.setText(playlistSong.getAlbumArtistOrArtist() + " - " + playlistSong.getAlbum());
        return rowView;
    }

    public void setNowPlayingIcon(int songId) {
        nowPlayingID = songId;
        notifyDataSetChanged();
    }
}
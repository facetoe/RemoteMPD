package com.facetoe.remotempd.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.facetoe.remotempd.R;
import org.a0z.mpd.Music;

import java.util.List;

/**
 * RemoteMPD
 * Created by facetoe on 20/02/14.
 */

public class MusicAdapter extends AbstractMPDArrayAdapter<Music> {
    public MusicAdapter(Context context, int itemLayoutID, List<Music> items) {
        super(context, itemLayoutID, items);
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
}
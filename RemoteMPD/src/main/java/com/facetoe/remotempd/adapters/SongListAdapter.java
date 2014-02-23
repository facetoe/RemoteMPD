package com.facetoe.remotempd.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.facetoe.remotempd.R;
import org.a0z.mpd.Item;
import org.a0z.mpd.Music;

import java.util.List;

/**
 * Created by facetoe on 31/12/13.
 */
public class SongListAdapter extends AbstractMPDArrayAdapter<Music> {

    public SongListAdapter(Context context, int itemLayoutID, List<Music> items) {
        super(context, itemLayoutID, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.song_list, parent, false);
        TextView title = (TextView) rowView.findViewById(R.id.name);
        TextView songLength = (TextView)rowView.findViewById(R.id.songLength);
        Music song = items.get(position);
        title.setText(song.getTitle());
        songLength.setText(song.getFormatedTime());

        return rowView;
    }
}

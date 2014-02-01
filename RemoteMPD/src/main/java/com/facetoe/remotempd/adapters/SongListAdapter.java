package com.facetoe.remotempd.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.facetoe.remotempd.R;
import org.a0z.mpd.Music;

import java.util.List;

/**
 * Created by facetoe on 31/12/13.
 */
public class SongListAdapter extends ArrayAdapter<Music> {
    private Context context;
    private List<Music> songList;
    public SongListAdapter(Context context, List<Music> songList) {
        super(context, R.layout.song_list, songList);
        this.songList = songList;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.song_list, parent, false);
        TextView title = (TextView) rowView.findViewById(R.id.label);
        title.setText(songList.get(position).getTitle());
        return rowView;
    }
}

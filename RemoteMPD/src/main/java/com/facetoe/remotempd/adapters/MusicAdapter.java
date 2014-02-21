package com.facetoe.remotempd.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import com.facetoe.remotempd.R;
import com.facetoe.remotempd.RMPDApplication;
import org.a0z.mpd.Music;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RemoteMPD
 * Created by facetoe on 20/02/14.
 */
public class MusicAdapter extends ArrayAdapter<Music> {
    private final Context context;
    private List<Music> items;
    private List<Music> storedItems;
    private static final String TAG = RMPDApplication.APP_PREFIX + "MusicAdapter";

    public MusicAdapter(Context context, List<Music> items) {
        super(context, R.layout.song_list, items);
        Log.i(TAG, "MusicAdapter created with " + items.size() + " songs.");
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

    public void updatePlaylist(List<Music> newItems) {
        storedItems.clear();
        storedItems.addAll(newItems);
        notifyDataSetChanged();
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

                    Log.i(TAG, "Found " + musicMatches.size() + " matches for " + constraint);

                    results.values = musicMatches;
                    results.count = musicMatches.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.count == 0) {
                    Log.i(TAG, "No results");
                    items = Collections.emptyList();
                    notifyDataSetChanged();
                } else {
                    Log.i(TAG, "Got " + results.count + " results");
                    items = (List<Music>) results.values;
                    notifyDataSetChanged();
                }
            }
        };
    }
}
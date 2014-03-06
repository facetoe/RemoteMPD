package com.facetoe.remotempd.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.facetoe.remotempd.R;
import org.a0z.mpd.Item;
import org.a0z.mpd.Music;

import java.util.List;

/**
 * RemoteMPD
 * Created by facetoe on 21/02/14.
 */
public class BrowserAdapter extends AbstractMPDArrayAdapter {

    public BrowserAdapter(Context context, List<Item> items) {
        super(context, R.layout.browser_item, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(itemLayoutID, parent, false);
        TextView name = (TextView) rowView.findViewById(R.id.name);
        TextView songLength = (TextView)rowView.findViewById(R.id.songLength);
        Item item = getItem(position);
        if(item instanceof Music) {
            Music song = (Music)item;
            name.setText(song.getTitle());
            songLength.setText(song.getFormatedTime());

        } else {
            name.setText(item.getName());
        }
        return rowView;
    }
}

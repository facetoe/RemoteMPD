package com.facetoe.remotempd.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.facetoe.remotempd.R;
import org.a0z.mpd.Artist;

import java.util.List;

/**
 * RemoteMPD
 * Created by facetoe on 21/02/14.
 */
public class ArtistAdapter extends AbstractMPDArrayAdapter<Artist> {

    public ArtistAdapter(Context context, int itemLayoutID, List<Artist> items) {
        super(context, itemLayoutID, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(R.layout.library_item, parent, false);
        TextView treeItem = (TextView) rowView.findViewById(itemLayoutID);

        Artist artist = items.get(position);
        treeItem.setText(artist.getName());
        return rowView;
    }
}

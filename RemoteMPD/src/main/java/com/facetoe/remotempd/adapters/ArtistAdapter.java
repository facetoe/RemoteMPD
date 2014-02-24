package com.facetoe.remotempd.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.facetoe.remotempd.R;
import org.a0z.mpd.Artist;
import org.a0z.mpd.Item;

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
        View rowView = inflater.inflate(itemLayoutID, parent, false);
        TextView txtView = (TextView) rowView.findViewById(R.id.txtView);
        Artist artist = getItem(position);
        if (artist != null) {
            txtView.setText(artist.getName());
        }
        return rowView;
    }
}

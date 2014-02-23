package com.facetoe.remotempd.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.facetoe.remotempd.R;
import org.a0z.mpd.Album;
import org.a0z.mpd.Item;

import java.util.List;

/**
 * RemoteMPD
 * Created by facetoe on 22/02/14.
 */
public class AlbumAdapter extends AbstractMPDArrayAdapter<Album> {

    public AlbumAdapter(Context context, int itemLayoutID, List<Album> items) {
        super(context, itemLayoutID, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(itemLayoutID, parent, false);
        TextView txtAlbum = (TextView) rowView.findViewById(R.id.txtAlbum);
        Album album = (Album)items.get(position);
        txtAlbum.setText(album.getName());
        return rowView;
    }
}

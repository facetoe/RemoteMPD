package com.facetoe.remotempd.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.facetoe.remotempd.R;
import com.facetoe.remotempd.RMPDApplication;
import com.facetoe.remotempd.adapters.AlbumAdapter;
import com.facetoe.remotempd.adapters.FilterTextWatcher;
import org.a0z.mpd.Album;
import org.a0z.mpd.Artist;
import org.a0z.mpd.Music;
import org.a0z.mpd.exception.MPDServerException;

import java.util.List;

/**
 * RemoteMPD
 * Created by facetoe on 23/02/14.
 */
public class ArtistAlbumsListFragment extends AbstractListFragment {
    private static final String TAG = RMPDApplication.APP_PREFIX + "ArtistAlbumsListFragment";
    Artist artist;
    LinearLayout spinnerLayout;

    ArtistAlbumsListFragment(SearchView searchView, Artist artist) {
        super(searchView);
        this.artist = artist;
    }

    @Override
    public void onStart() {
        super.onStart();
        new LoadAlbumsTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.filterable_list, container, false);
        spinnerLayout = (LinearLayout)rootView.findViewById(R.id.filterableListSpinnerLayout);

        adapter = new AlbumAdapter(getActivity(), R.layout.album_item, entries);
        listItems = (ListView) rootView.findViewById(R.id.listItems);
        listItems.setAdapter(adapter);
        listItems.setOnItemClickListener(this);

        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Album album = (Album) adapter.getItem(position);
        Log.i(TAG, "Clicked album: " + album.getName());
        SongListFragment songListFragment = new SongListFragment(searchView, album);
        replaceWithFragment(songListFragment);
    }

    class LoadAlbumsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            spinnerLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            spinnerLayout.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Log.i(TAG, "Getting albums for " + artist.getName());
                entries.clear();
                entries.addAll(mpd.getAlbums(artist, true));
                Log.i(TAG, "Found " + entries + " albums for artist");
                updateEntries(entries);
            } catch (MPDServerException e) {
                Log.e(TAG, "Error loading albums", e);
            }
            return null;
        }
    }
}
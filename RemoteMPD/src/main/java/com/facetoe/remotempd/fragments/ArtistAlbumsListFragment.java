package com.facetoe.remotempd.fragments;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SearchView;
import com.facetoe.remotempd.R;
import com.facetoe.remotempd.RMPDApplication;
import com.facetoe.remotempd.adapters.AbstractMPDArrayAdapter;
import com.facetoe.remotempd.adapters.AlbumAdapter;
import org.a0z.mpd.Album;
import org.a0z.mpd.Artist;
import org.a0z.mpd.exception.MPDServerException;

/**
 * RemoteMPD
 * Created by facetoe on 23/02/14.
 */
public class ArtistAlbumsListFragment extends AbstractListFragment {
    private static final String TAG = RMPDApplication.APP_PREFIX + "ArtistAlbumsListFragment";
    private final Artist artist;

    ArtistAlbumsListFragment(SearchView searchView, Artist artist) {
        super(searchView);
        this.artist = artist;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (entries.size() == 0) {
            new LoadAlbumsTask().execute();
        }
    }

    @Override
    protected AbstractMPDArrayAdapter getAdapter() {
        //noinspection unchecked
        return new AlbumAdapter(getActivity(), R.layout.list_item, entries);
    }

    @Override
    protected String getTitle() {
        return artist.getName();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Album album = (Album) adapter.getItem(position);
        Log.d(TAG, "Clicked album: " + album.getName());
        SongListFragment songListFragment = new SongListFragment(searchView, album);
        replaceWithFragment(songListFragment);
    }

    private class LoadAlbumsTask extends AsyncTask<Void, Void, Void> {
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
                Log.d(TAG, "Getting albums for " + artist.getName());
                updateEntries(mpd.getAlbums(artist, true));
            } catch (MPDServerException e) {
                app.notifyEvent(RMPDApplication.Event.CONNECTION_FAILED);
            }
            return null;
        }
    }
}
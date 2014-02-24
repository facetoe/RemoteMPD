package com.facetoe.remotempd.fragments;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SearchView;
import com.facetoe.remotempd.R;
import com.facetoe.remotempd.RMPDApplication;
import com.facetoe.remotempd.adapters.AbstractMPDArrayAdapter;
import com.facetoe.remotempd.adapters.ArtistAdapter;
import com.facetoe.remotempd.helpers.MPDAsyncHelper;
import org.a0z.mpd.Artist;
import org.a0z.mpd.ConnectionListener;
import org.a0z.mpd.exception.MPDServerException;


/**
 * RemoteMPD
 * Created by facetoe on 21/02/14.
 */


public class ArtistListFragment extends AbstractListFragment implements ConnectionListener {
    private final MPDAsyncHelper asyncHelper = RMPDApplication.getInstance().getAsyncHelper();
    private final String TAG = RMPDApplication.APP_PREFIX + "ArtistListFragment";

    public ArtistListFragment(SearchView searchView) {
        super(searchView);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.artistFragmentTitle);
    }

    @Override
    protected AbstractMPDArrayAdapter getAdapter() {
        //noinspection unchecked
        return new ArtistAdapter(getActivity(), R.layout.list_item, entries);
    }

    @Override
    public void onStart() {
        super.onStart();
        asyncHelper.addConnectionListener(this);
        if (asyncHelper.oMPD.isConnected()) {
            new LoadArtistTask().execute();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        asyncHelper.removeConnectionListener(this);
    }

    @Override
    public void connectionFailed(String message) {

    }

    @Override
    public void connectionSucceeded(String message) {
        if (entries.size() == 0) {
            new LoadArtistTask().execute();
        }
    }

    private void addArtists() {
        Log.i(TAG, "addArtists()");
        try {
            if (entries.size() == 0) {
                updateEntries(mpd.getArtists());
            }
        } catch (MPDServerException e) {
            app.notifyEvent(RMPDApplication.Event.CONNECTION_FAILED);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Artist artist = (Artist) adapter.getItem(position);
        Log.d(TAG, "Artist clicked: " + artist.getName());
        ArtistAlbumsListFragment albumFragment = new ArtistAlbumsListFragment(searchView, artist);
        replaceWithFragment(albumFragment);
    }

    private class LoadArtistTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            spinnerLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void[] params) {
            addArtists();
            return null;
        }

        @Override
        protected void onPostExecute(Void o) {
            spinnerLayout.setVisibility(View.GONE);
        }
    }
}

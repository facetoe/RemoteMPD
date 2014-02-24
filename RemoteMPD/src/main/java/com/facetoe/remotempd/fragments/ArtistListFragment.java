package com.facetoe.remotempd.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.facetoe.remotempd.R;
import com.facetoe.remotempd.RMPDApplication;
import com.facetoe.remotempd.adapters.*;
import com.facetoe.remotempd.helpers.MPDAsyncHelper;
import org.a0z.mpd.*;
import org.a0z.mpd.exception.MPDServerException;

import java.util.ArrayList;
import java.util.List;


/**
 * RemoteMPD
 * Created by facetoe on 21/02/14.
 */


public class ArtistListFragment extends AbstractListFragment implements ConnectionListener {
    MPDAsyncHelper asyncHelper = RMPDApplication.getInstance().getAsyncHelper();
    private String TAG = RMPDApplication.APP_PREFIX + "ArtistListFragment";
    private LinearLayout spinnerLayout;

    public ArtistListFragment(SearchView searchView) {
        super(searchView);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.filterable_list, container, false);

        adapter = new ArtistAdapter(getActivity(), R.layout.list_item, entries);
        spinnerLayout = (LinearLayout)rootView.findViewById(R.id.filterableListSpinnerLayout);

        listItems = (ListView) rootView.findViewById(R.id.listItems);
        listItems.setOnItemClickListener(this);
        listItems.setAdapter(adapter);

        setRetainInstance(true);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        asyncHelper.addConnectionListener(this);
        if(asyncHelper.oMPD.isConnected()) {
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
        new LoadArtistTask().execute();
    }

    private void addArtists() {
        Log.i(TAG, "addArtists()");
        try {
            if(entries.size() == 0) {
                List<Artist> artists = mpd.getArtists();
                Log.i(TAG, "Adding " + artists.size() + " artists.");
                entries.addAll(artists);
                updateEntries(entries);
            }
        } catch (MPDServerException e) {
            Log.e(TAG, "Error updating entries: " + e.getMessage());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Artist artist = (Artist) entries.get(position);
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

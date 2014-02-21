package com.facetoe.remotempd.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.facetoe.remotempd.R;
import com.facetoe.remotempd.RMPDApplication;
import com.facetoe.remotempd.adapters.AbstractMPDArrayAdapter;
import com.facetoe.remotempd.adapters.ArtistAdapter;
import com.facetoe.remotempd.helpers.MPDAsyncHelper;
import org.a0z.mpd.Artist;
import org.a0z.mpd.ConnectionListener;
import org.a0z.mpd.MPD;
import org.a0z.mpd.exception.MPDServerException;

import java.util.ArrayList;
import java.util.List;



/**
 * RemoteMPD
 * Created by facetoe on 21/02/14.
 */
public class LibraryFragment extends Fragment implements ConnectionListener, AdapterView.OnItemClickListener {
    EditText txtLibrarySearch;
    ListView listLibrary;
    List<Artist> entries;
    AbstractMPDArrayAdapter adapter;


    MPD mpd = RMPDApplication.getInstance().getMpd();
    MPDAsyncHelper asyncHelper = RMPDApplication.getInstance().getAsyncHelper();
    private String TAG = RMPDApplication.APP_PREFIX + "LibraryFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.library, container, false);

        txtLibrarySearch = (EditText) rootView.findViewById(R.id.txtLibrarySearch);
        txtLibrarySearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        listLibrary = (ListView) rootView.findViewById(R.id.listLibrary);

        entries = new ArrayList<Artist>();
        adapter = new ArtistAdapter(getActivity(), R.id.txtLibraryItem, entries);

        listLibrary.setAdapter(adapter);
        listLibrary.setOnItemClickListener(this);

        asyncHelper.addConnectionListener(this);

        setRetainInstance(true);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateEntries();
    }

    private void updateEntries() {
        if (entries.size() == 0) {
            try {
                entries.addAll(mpd.getArtists(true));
                updateAdapter();
            } catch (MPDServerException e) {
                Log.e(TAG, "Error updating entries: " + e.getMessage());
            }
        } else {
            updateAdapter();
        }
    }

    @Override
    public void connectionFailed(String message) {

    }

    @Override
    public void connectionSucceeded(String message) {
        updateEntries();
    }

    private void updateAdapter() {
        Activity parentActivity = getActivity();
        if (parentActivity != null) {
            parentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "Updating adapter with " + entries.size() + " entries");
                    adapter.resetEntries(entries);
                }
            });
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i(TAG, "Item clicked: " + position);
        final Artist artist =(Artist) adapter.getItem(position);
        if (artist == null) {
            Log.w(TAG, "Artist was null");
            return;
        }
        Log.i(TAG, "Item: " + artist.subText());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "Entries: " + mpd.getAlbums(artist));
                } catch (MPDServerException e) {
                    Log.e(TAG, "Error getting albums: " + e.getMessage());
                }
            }
        }).start();

    }
}

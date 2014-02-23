package com.facetoe.remotempd.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
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


class SongListFragment extends AbstractListFragment {
    private static final String TAG = RMPDApplication.APP_PREFIX + "SongListFragment";

    SongListFragment(List<Music> songs) {
        this.entries = songs;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.filterable_list, container, false);
        adapter = new SongListAdapter(getActivity(), R.layout.song_list, entries);

        listItems = (ListView) rootView.findViewById(R.id.listItems);
        listItems.setAdapter(adapter);
        listItems.setOnItemClickListener(this);

        txtSearch = (EditText) rootView.findViewById(R.id.txtSearch);
        txtSearch.addTextChangedListener(new FilterTextWatcher(adapter));
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Music song = (Music)adapter.getItem(position);
        try {
            mpd.add(song);
        } catch (MPDServerException e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }
}

class ArtistAlbumsListFragment extends AbstractListFragment {
    private static final String TAG = RMPDApplication.APP_PREFIX + "ArtistAlbumsListFragment";

    ArtistAlbumsListFragment(List<Album> albums) {
        this.entries = albums;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.filterable_list, container, false);
        adapter = new AlbumAdapter(getActivity(), R.layout.album_item, entries);

        txtSearch = (EditText) rootView.findViewById(R.id.txtSearch);
        listItems = (ListView) rootView.findViewById(R.id.listItems);
        listItems.setAdapter(adapter);
        listItems.setOnItemClickListener(this);

        txtSearch.addTextChangedListener(new FilterTextWatcher(adapter));
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Album album = (Album) adapter.getItem(position);
        Log.i(TAG, "Clicked album: " + album.getName());

        try {
            List<Music> songs = mpd.getSongs(album.getArtist(), album);
            SongListFragment songListFragment = new SongListFragment(songs);
            replaceWithFragment(songListFragment);
        } catch (MPDServerException e) {
            Log.e(TAG, "Bad things: " + e.getMessage());
        }

    }
}

abstract class AbstractListFragment extends Fragment implements AdapterView.OnItemClickListener {
    protected final MPD mpd = RMPDApplication.getInstance().getMpd();
    protected AbstractMPDArrayAdapter adapter;
    protected EditText txtSearch;
    protected ListView listItems;
    protected List entries = new ArrayList<Item>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void replaceWithFragment(Fragment fragment) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.filterableListContainer, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }
}

public class ArtistListFragment extends AbstractListFragment implements ConnectionListener {
    MPDAsyncHelper asyncHelper = RMPDApplication.getInstance().getAsyncHelper();
    private String TAG = RMPDApplication.APP_PREFIX + "ArtistListFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.filterable_list, container, false);

        txtSearch = (EditText) rootView.findViewById(R.id.txtSearch);
        txtSearch.addTextChangedListener(new FilterTextWatcher(adapter));

        adapter = new ArtistAdapter(getActivity(), R.layout.list_item, entries);
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
            addArtists();
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
        addArtists();
    }

    private void addArtists() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    entries.addAll(mpd.getArtists());
                    updateArtistList();
                } catch (MPDServerException e) {
                    Log.e(TAG, "Error updating entries: " + e.getMessage());
                }
            }
        }).start();
    }

    private void updateArtistList() {
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
        final Artist artist = (Artist) entries.get(position);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Album> albums = mpd.getAlbums(artist, true);
                    ArtistAlbumsListFragment albumFragment = new ArtistAlbumsListFragment(albums);
                    replaceWithFragment(albumFragment);
                } catch (MPDServerException e) {
                    Log.e(TAG, "Error: ", e);
                }
            }
        }).start();
    }
}

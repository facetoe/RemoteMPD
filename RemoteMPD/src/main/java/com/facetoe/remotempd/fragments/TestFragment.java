package com.facetoe.remotempd.fragments;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SearchView;
import com.facetoe.remotempd.RMPDApplication;
import org.a0z.mpd.*;
import org.a0z.mpd.exception.MPDServerException;

import java.util.List;
import java.util.Stack;

/**
 * RemoteMPD
 * Created by facetoe on 26/02/14.
 */
public class TestFragment extends AbstractListFragment implements ConnectionListener {

    private static final String TAG = RMPDApplication.APP_PREFIX + "TestFragment";
    Stack<List<? extends Item>> backStack = new Stack<List<? extends Item>>();
    List<? extends Item> previousItems;

    public TestFragment(SearchView searchView) {
        super(searchView);
    }

    public boolean canGoBack() {
        return !backStack.empty();
    }

    public void goBack() {
        List<? extends Item> items = backStack.pop();
        previousItems = items;
        updateEntries(items);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Item clickedItem = adapter.getItem(position);
        if (clickedItem instanceof Artist || clickedItem instanceof Album) {
            new PopulateListTask().execute(clickedItem);
        } else if (clickedItem instanceof Music) {
            addSong(clickedItem);
        } else {
            Log.wtf(TAG, "What the... " + clickedItem);
        }
    }

    private void addSong(Item clickedItem) {
        Music song = (Music)clickedItem;
        add(song, false, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        app.getAsyncHelper().addConnectionListener(this);
        if (mpd.isConnected()) {
            new PopulateListTask().execute();
        }
    }

    @Override
    public void connectionFailed(String message) {

    }

    @Override
    public void connectionSucceeded(String message) {
        if (entries.size() == 0) {
            new PopulateListTask().execute();
        }
    }

    private class PopulateListTask extends AsyncTask<Item, Void, Void> {

        @Override
        protected void onPreExecute() {
            spinnerLayout.setVisibility(View.VISIBLE);
        }
        @Override
        protected Void doInBackground(Item[] params) {
            if(params.length == 0) {
                addArtists();
                return null;
            }
            Item item = params[0];
            if(item instanceof Artist) {
                showAlbumsForArtist((Artist) item);
            } else if(item instanceof Album) {
                showSongsForAlbum((Album) item);
            } else {
                Log.wtf(TAG, "wtf... " + item);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void o) {
            spinnerLayout.setVisibility(View.GONE);
        }

    }

    private void addArtists() {
        Log.i(TAG, "addArtists()");
        try {
            if (entries.size() == 0) {
                List<Artist> artists = mpd.getArtists();
                previousItems = artists;
                updateEntries(artists);
            }
        } catch (MPDServerException e) {
            app.notifyEvent(RMPDApplication.Event.CONNECTION_FAILED);
        }
    }

    private void showAlbumsForArtist(Artist item) {
        try {
            List<Album> albums = mpd.getAlbums(item);
            displayAndAddToBackstack(albums);
        } catch (MPDServerException e) {
            Log.e(TAG, "Fail: ", e);
        }
    }

    private void showSongsForAlbum(Album album) {
        try {
            List<Music> songs = mpd.getSongs(album.getArtist(), album);
            displayAndAddToBackstack(songs);
        } catch (MPDServerException e) {
            Log.e(TAG, "Error", e);
        }
    }

    private void displayAndAddToBackstack(List<? extends Item> items) {
        backStack.push(previousItems);
        previousItems = items;
        updateEntries(items);
    }
}

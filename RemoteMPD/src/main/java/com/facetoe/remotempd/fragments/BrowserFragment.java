package com.facetoe.remotempd.fragments;

import android.os.AsyncTask;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.facetoe.remotempd.RMPDApplication;
import org.a0z.mpd.*;
import org.a0z.mpd.exception.MPDServerException;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * RemoteMPD
 * Created by facetoe on 26/02/14.
 */
public class BrowserFragment extends AbstractListFragment implements ConnectionListener {
    private static final String TAG = RMPDApplication.APP_PREFIX + "BrowserFragment";
    Stack<CachedItems> backStack = new Stack<CachedItems>();
    CachedItems currentItems;
    String title = "Artists";

    private static final int ADD_ITEM = 1;
    private static final int ADD_AND_REPLACE = 2;
    private static final int ADD_REPLACE_AND_PLAY = 3;
    private static final int ADD_AND_PLAY = 4;

    @Override
    public void onStart() {
        Log.i(TAG, "onStart()");
        super.onStart();
        app.getAsyncHelper().addConnectionListener(this);
        if (mpd.isConnected()) {
            new PopulateListTask().execute(); // This will populate with artists.
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getUserVisibleHint()) {
            onVisible();
        }
    }

    public boolean canGoBack() {
        return !backStack.empty();
    }

    @Override
    public void onVisible() {
        setTitle(title);
    }

    public void goBack() {
        currentItems = backStack.pop();
        updateEntries(currentItems.getItems());
        title = currentItems.getTitle();
        setTitle(title);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == com.facetoe.remotempd.R.id.listItems) {
            ListView lv = (ListView) v;
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Item item = (Item) lv.getItemAtPosition(acmi.position);

            menu.add(Menu.NONE, ADD_ITEM, Menu.NONE, "Add " + getItemName(item));
            menu.add(Menu.NONE, ADD_AND_REPLACE, Menu.NONE, "Add and replace " + getItemName(item));
            menu.add(Menu.NONE, ADD_REPLACE_AND_PLAY, Menu.NONE, "Add replace and play");
            menu.add(Menu.NONE, ADD_AND_PLAY, Menu.NONE, "Add and play");
        }
    }

    private String getItemName(Item item) {
        if (item instanceof Artist) {
            return "artist";
        } else if (item instanceof Album) {
            return "album";
        } else if (item instanceof Music) {
            return "song";
        } else {
            Log.e(TAG, "Unknown item passed to getItemName()");
            return "";
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        // If this fragment is not visible, don't consume the event. Return false to let
        // whichever fragment is currently visible deal with it.
        if(!getUserVisibleHint()) {
            return false;
        }


        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null) {
            Log.e(TAG, "AdapterContextMenuInfo was null");
            return true;
        }

        Item selectedItem = adapter.getItem(info.position);
        switch (item.getItemId()) {
            case ADD_ITEM:
                Log.d(TAG, "Add: " + selectedItem);
                addItem(selectedItem, false, false);
                break;

            case ADD_AND_REPLACE:
                Log.d(TAG, "Add and replace: " + selectedItem);
                addItem(selectedItem, true, false);
                break;

            case ADD_REPLACE_AND_PLAY:
                Log.d(TAG, "Add, replace and play: " + selectedItem);
                addItem(selectedItem, true, true);
                break;

            case ADD_AND_PLAY:
                Log.d(TAG, "Add and play: " + selectedItem);
                addItem(selectedItem, false, true);
                break;

            default:
                Log.w(TAG, "Unknown: " + item.getItemId());
                break;
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Hide the keyboard and SearchView if the user was conducting a search.
        hideKeyboardAndCollapseSearchView();

        final Item clickedItem = adapter.getItem(position);
        if (clickedItem instanceof Artist || clickedItem instanceof Album) {
            new PopulateListTask().execute(clickedItem);
        } else if (clickedItem instanceof Music) {
            addSong(clickedItem);
        } else {
            Log.wtf(TAG, "What the... " + clickedItem);
        }
    }

    private void addSong(final Item clickedItem) {
        Music song = (Music) clickedItem;
        add(song, false, false);
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
            if (params.length == 0) {
                showArtists();
                return null;
            } else if (params.length > 1) {
                Log.w(TAG, "Too many parameters passed to PopulateListTask. Expected 1, received: " + params.length);
            }

            Item item = params[0];
            if (item instanceof Artist) {
                showAlbumsForArtist((Artist) item);
            } else if (item instanceof Album) {
                showSongsForAlbum((Album) item);
            } else {
                Log.wtf(TAG, "Unknown item passed to PopulateListTask: " + item);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void o) {
            spinnerLayout.setVisibility(View.GONE);
        }
    }

    private void showArtists() {
        try {
            if (entries.size() == 0) {
                List<Artist> artists = mpd.getArtists();
                currentItems = new CachedItems(artists, "Artists");
                updateEntries(artists);
            }
        } catch (MPDServerException e) {
            app.notifyEvent(RMPDApplication.Event.CONNECTION_FAILED);
        }
    }

    private void showAlbumsForArtist(Artist artist) {
        title = artist.getName();
        setTitle(title);
        try {
            List<Album> albums = mpd.getAlbums(artist);
            displayAndAddToBackstack(albums, artist);
        } catch (MPDServerException e) {
            Log.e(TAG, "Fail: ", e);
        }
    }

    private void showSongsForAlbum(Album album) {
        title = album.getName();
        setTitle(title);
        try {
            List<Music> songs = mpd.getSongs(album.getArtist(), album);
            displayAndAddToBackstack(songs, album);
        } catch (MPDServerException e) {
            Log.e(TAG, "Error", e);
        }
        onVisible();
    }

    private void displayAndAddToBackstack(List<? extends Item> items, Item item) {
        backStack.push(currentItems);
        currentItems = new CachedItems(items, item.getName());
        updateEntries(items);
    }

    private class CachedItems {
        private List<? extends Item> items;
        private String title;

        private CachedItems(List<? extends Item> items, String title) {
            this.items = items;
            this.title = title;
        }

        public List<? extends Item> getItems() {
            return items;
        }

        public String getTitle() {
            return title;
        }
    }

    protected void addItem(Item item, boolean replace, boolean play) {
        if (item instanceof Artist) {
            add((Artist) item, replace, play);
        } else if (item instanceof Album) {
            add((Album) item, replace, play);
        } else if (item instanceof Music) {
            add((Music) item, replace, play);
        } else {
            Log.w(TAG, "Unknown item passed to addItem()");
        }
    }

    // This method is necessary as MPD's add Artist command only seems to add the first album by the artist.
    protected void add(final Artist artist, final boolean replace, final boolean play) {
        final Toast toast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(replace) {
                        mpd.getPlaylist().clear();
                    }

                    List<Music> songs = getSongsForArtist(artist);
                    mpd.getPlaylist().addAll(songs);

                    if(play) {
                        if(songs.size() > 0) {
                            Music firstSong = songs.get(0);
                            mpd.skipToId(firstSong.getSongId());
                        }
                    }
                    toast.setText("Added " + artist.getName());
                    toast.show();
                } catch (MPDServerException e) {
                    app.notifyEvent(RMPDApplication.Event.CONNECTION_FAILED);
                }
            }
        }).start();
    }

    // If this method turns out to be really slow it might be faster to queue up the commands
    // and send them all at once.
    private List<Music> getSongsForArtist(Artist artist) throws MPDServerException {
        List<Album> albums = mpd.getAlbums(artist);
        List<Music> songs = new ArrayList<Music>();
        for (Album album : albums) {
            Log.i(TAG, "Adding: " + album);
            songs.addAll(mpd.getSongs(artist, album));
        }
        return songs;
    }

    protected void add(final Album album, final boolean replace, final boolean play) {
        final Toast toast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mpd.add(album, replace, play);
                    toast.setText("Added " + album.getName());
                    toast.show();
                } catch (MPDServerException e) {
                    app.notifyEvent(RMPDApplication.Event.CONNECTION_FAILED);
                }
            }
        }).start();
    }

    protected void add(final Music song, final boolean replace, final boolean play) {
        final Toast toast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mpd.add(song, replace, play);
                    toast.setText("Added " + song.getName());
                    toast.show();
                } catch (MPDServerException e) {
                    app.notifyEvent(RMPDApplication.Event.CONNECTION_FAILED);
                }
            }
        }).start();
    }
}

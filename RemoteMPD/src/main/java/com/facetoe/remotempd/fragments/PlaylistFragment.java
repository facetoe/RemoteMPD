package com.facetoe.remotempd.fragments;

import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import com.facetoe.remotempd.RMPDApplication;
import org.a0z.mpd.Item;
import org.a0z.mpd.MPDPlaylist;
import org.a0z.mpd.MPDStatus;
import org.a0z.mpd.Music;
import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.exception.MPDServerException;


/**
 * RemoteMPD
 * Created by facetoe on 11/02/14.
 */
public class PlaylistFragment extends AbstractListFragment implements StatusChangeListener {

    private final MPDPlaylist playlist = app.getMpd().getPlaylist();
    private final String TAG = RMPDApplication.APP_PREFIX + "PlaylistFragment";

    private static final int REMOVE = 1;

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart()");
        app.getAsyncHelper().addStatusChangeListener(this);
        if (entries.size() == 0) {
            updateEntries(playlist.getMusicList());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            onVisible();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        app.getAsyncHelper().removeStatusChangeListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Music song = (Music) adapter.getItem(position);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mpd.skipToId(song.getSongId());
                } catch (MPDServerException e) {
                    app.notifyEvent(RMPDApplication.Event.CONNECTION_FAILED, e.getMessage());
                }
            }
        }).start();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        if (v.getId() == com.facetoe.remotempd.R.id.listItems) {
            menu.add(Menu.NONE, REMOVE, Menu.NONE, "Remove");
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
        if (item.getItemId() == REMOVE) {
            Music song = (Music) selectedItem;
            removeSong(song);
        }
        return true;
    }


    private void removeSong(final Music song) {
        final Toast toast = Toast.makeText(getActivity(), "Removed " + song.getTitle(), Toast.LENGTH_SHORT);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    playlist.removeById(song.getSongId());
                    toast.show();
                } catch (MPDServerException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onVisible() {
        setTitle("Playlist");
    }

    @Override
    public void volumeChanged(MPDStatus mpdStatus, int oldVolume) {

    }

    @Override
    public void playlistChanged(MPDStatus mpdStatus, int oldPlaylistVersion) {
        updateEntries(playlist.getMusicList());
    }

    @Override
    public void trackChanged(MPDStatus mpdStatus, int oldTrack) {

    }

    @Override
    public void stateChanged(MPDStatus mpdStatus, String oldState) {

    }

    @Override
    public void repeatChanged(boolean repeating) {

    }

    @Override
    public void randomChanged(boolean random) {

    }

    @Override
    public void connectionStateChanged(boolean connected, boolean connectionLost) {

    }

    @Override
    public void libraryStateChanged(boolean updating) {

    }
}

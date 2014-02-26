package com.facetoe.remotempd.fragments;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.facetoe.remotempd.R;
import com.facetoe.remotempd.RMPDApplication;
import com.facetoe.remotempd.adapters.AbstractMPDArrayAdapter;
import com.facetoe.remotempd.adapters.ItemAdapter;
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

    public PlaylistFragment(SearchView searchView) {
        super(searchView);
    }

    @Override
    public void onStart() {
        super.onStart();
        app.getAsyncHelper().addStatusChangeListener(this);
        if(entries.size() == 0) {
            updateEntries(playlist.getMusicList());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        app.getAsyncHelper().removeStatusChangeListener(this);
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Music song = (Music)adapter.getItem(position);
        try {
            mpd.skipToId(song.getSongId());
        } catch (MPDServerException e) {
            app.notifyEvent(RMPDApplication.Event.CONNECTION_FAILED, e.getMessage());
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.i(TAG, "Context menu");
        return true;
    }
}

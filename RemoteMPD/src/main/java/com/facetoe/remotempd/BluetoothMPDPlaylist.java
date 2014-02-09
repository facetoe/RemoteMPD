package com.facetoe.remotempd;

import android.util.Log;
import org.a0z.mpd.MPDStatus;
import org.a0z.mpd.Music;
import org.a0z.mpd.MusicList;
import org.a0z.mpd.Playlist;
import org.a0z.mpd.event.AbstractStatusChangeListener;
import org.a0z.mpd.exception.MPDServerException;

import java.util.ArrayList;
import java.util.List;

/**
 * RemoteMPD
 * Created by facetoe on 9/02/14.
 */
public class BluetoothMPDPlaylist extends AbstractStatusChangeListener {

    private MusicList list;
    private int lastPlaylistVersion = -1;
    private boolean firstRefreash = true;
    private static final boolean DEBUG = true;
    private MPDStatus status;

    BluetoothMPDManager btManager;
    private String TAG = RMPDApplication.APP_PREFIX + "BluetoothMPDPlaylist";

    public BluetoothMPDPlaylist(BluetoothMPDManager btManager) {
        this.btManager = btManager;
    }

    @Override
    public void playlistChanged(MPDStatus mpdStatus, int oldPlaylistVersion) {
        Log.i(TAG, "Playlist changed: " + oldPlaylistVersion);
        btManager.sendCommand(new PlaylistCommand(PlaylistCommand.MPD_CMD_PLAYLIST_CHANGES, Integer.toString(oldPlaylistVersion)));
        status = mpdStatus;
        lastPlaylistVersion = status.getPlaylistVersion();
    }
}

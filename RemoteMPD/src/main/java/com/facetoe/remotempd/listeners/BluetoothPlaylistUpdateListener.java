package com.facetoe.remotempd.listeners;

import com.facetoe.remotempd.MPDCachedPlaylist;


/**
 * Listener to be alerted on MPDCachedPlaylist updates from the server.
 */
public interface BluetoothPlaylistUpdateListener {
    void updatePlayList(MPDCachedPlaylist newSongList);
}
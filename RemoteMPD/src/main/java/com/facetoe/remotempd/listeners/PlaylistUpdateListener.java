package com.facetoe.remotempd.listeners;

import org.a0z.mpd.Music;

import java.util.List;

/**
 * RemoteMPD
 * Created by facetoe on 11/02/14.
 */

/**
 * Called when a playlist is updated from the BluetoothServer.
 * Only returns the songs that have changed.
 */
public interface PlaylistUpdateListener {
    void updatePlaylist(List<Music> changes);
}
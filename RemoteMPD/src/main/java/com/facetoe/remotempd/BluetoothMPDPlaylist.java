package com.facetoe.remotempd;

import android.util.Log;
import com.facetoe.remotempd.exceptions.NoBluetoothServerConnectionException;
import org.a0z.mpd.FilesystemTreeEntry;
import org.a0z.mpd.MPDStatus;
import org.a0z.mpd.Music;
import org.a0z.mpd.MusicList;
import org.a0z.mpd.event.AbstractStatusChangeListener;
import org.a0z.mpd.exception.MPDServerException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

interface WillBeAbstractBaseClass {
    int refresh();

    public void addAll(Collection<Music> c);
    public void add(FilesystemTreeEntry entry);
}
/**
 * RemoteMPD
 * Created by facetoe on 9/02/14.
 */
public class BluetoothMPDPlaylist extends AbstractStatusChangeListener implements
        PlaylistUpdateListener {

    private String TAG = RMPDApplication.APP_PREFIX + "BluetoothMPDPlaylist";
    RMPDApplication app = RMPDApplication.getInstance();

    private static final String LAST_PLAYLIST_VERSION = "lastPlaylistVersion";

    private MusicList list = new MusicList(); //TODO This should be loaded from JSON
    private int lastPlaylistVersion;
    private boolean firstRefreash = true;

    private static final boolean DEBUG = true;
    private MPDStatus status;
    BluetoothConnection btConnection;

    public BluetoothMPDPlaylist(BluetoothConnection btConnection) {
        this.btConnection = btConnection;
        lastPlaylistVersion = app.getPref(LAST_PLAYLIST_VERSION, -1);
    }

    /**
     * Adds a <code>Collection</code> of <code>Music</code> to playlist.
     *
     * @param c
     *           <code>Collection</code> of <code>Music</code> to be added to playlist.
     * @throws org.a0z.mpd.exception.MPDServerException
     *            if an error occur while contacting server.
     * @see Music
     */
    public void addAll(Collection<Music> c) throws NoBluetoothServerConnectionException {
        for (Music m : c)
            btConnection.queueCommand(BTServerCommand.MPD_CMD_PLAYLIST_ADD, m.getFullpath());

        btConnection.sendCommandQueue();
        refresh();
    }

    /**
     * Adds a music to playlist.
     *
     * @param entry
     *           music/directory/playlist to be added.
     * @throws MPDServerException
     *            if an error occur while contacting server.
     */
    public void add(FilesystemTreeEntry entry) throws NoBluetoothServerConnectionException {
        btConnection.sendCommand(BTServerCommand.MPD_CMD_PLAYLIST_ADD, entry.getFullpath());
        this.refresh();
    }



    @Override
    public void playlistChanged(MPDStatus mpdStatus, int oldPlaylistVersion) {
        Log.i(TAG, "Playlist changed from " + oldPlaylistVersion + " to " + mpdStatus.getPlaylistVersion());
        status = mpdStatus;
        refresh();
    }

    /**
     * This command works differently than in JMPDComm. If the cached playlistVersion doesn't match
     * the latest one then a request is sent to the server asking for updates. The server responds
     * with a List of changes, which are then updated in the updatePlaylist() method.
    **/
    private void refresh() {
        if (lastPlaylistVersion != status.getPlaylistVersion()) {
            try {
                btConnection.sendCommand(new BTServerCommand(
                        BTServerCommand.MPD_CMD_PLAYLIST_CHANGES,
                        Integer.toString(lastPlaylistVersion)));
            } catch (NoBluetoothServerConnectionException e) {
                app.notifyEvent(RMPDApplication.Event.CONNECTION_FAILED,
                        "Connection to Bluetooth server failed");
            }
        }
        setAndSaveVersion();
    }

    private void setAndSaveVersion() {
        lastPlaylistVersion = status.getPlaylistVersion();
        app.setPref(LAST_PLAYLIST_VERSION, lastPlaylistVersion);
    }

    @Override
    public void updatePlaylist(List<Music> changes) {
        Log.d(TAG, "Updating playlist with " + changes.size() + " changed songs.");
        int newLength = status.getPlaylistLength();
        int oldLength = this.list.size();
        List<Music> newPlaylist = new ArrayList<Music>(newLength+1);

        newPlaylist.addAll(this.list.subList(0 , newLength < oldLength ? newLength : oldLength));

        for(int i = newLength - oldLength; i > 0; i--)
            newPlaylist.add(null);

        for( Music song : changes ) {
            if (newPlaylist.size() > song.getPos() && song.getPos() > -1) {
                newPlaylist.set(song.getPos(), song);
            }
        }

        this.list.clear();
        this.list.addAll(newPlaylist);
    }
}

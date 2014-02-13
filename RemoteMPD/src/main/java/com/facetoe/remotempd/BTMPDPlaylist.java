package com.facetoe.remotempd;

import android.util.Log;
import com.facetoe.remotempd.exceptions.NoBluetoothServerConnectionException;
import com.facetoe.remotempd.listeners.PlaylistChangeListener;
import org.a0z.mpd.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * RemoteMPD
 * Created by facetoe on 9/02/14.
 */
public class BTMPDPlaylist extends AbstractMPDPlaylist implements
        PlaylistChangeListener {

    private final String TAG = RMPDApplication.APP_PREFIX + "BTMPDPlaylist";
    private final RMPDApplication app = RMPDApplication.getInstance();

    private static final String LAST_PLAYLIST_VERSION = "lastPlaylistVersion";

    private final MusicList list = new MusicList(); //TODO This should be loaded from JSON
    private int lastPlaylistVersion;

    private static final boolean DEBUG = true;
    private MPDStatus status;
    private final BluetoothConnection btConnection;

    public BTMPDPlaylist(BluetoothConnection btConnection) {
        this.btConnection = btConnection;
        //lastPlaylistVersion = app.getPref(LAST_PLAYLIST_VERSION, -1);
        lastPlaylistVersion = -1;
    }

    /**
     * Adds a <code>Collection</code> of <code>Music</code> to playlist.
     *
     * @param c <code>Collection</code> of <code>Music</code> to be added to playlist.
     * @throws NoBluetoothServerConnectionException if an error occur while contacting server.
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
     * @param entry music/directory/playlist to be added.
     * @throws com.facetoe.remotempd.exceptions.NoBluetoothServerConnectionException if an error occur while contacting server.
     */
    public void add(FilesystemTreeEntry entry) throws NoBluetoothServerConnectionException {
        btConnection.sendCommand(BTServerCommand.MPD_CMD_PLAYLIST_ADD, entry.getFullpath());
        this.refresh();
    }

    /**
     * Adds a stream to playlist.
     *
     * @param url streams URL
     * @throws NoBluetoothServerConnectionException
     */
    public void add(URL url) throws NoBluetoothServerConnectionException {
        btConnection.sendCommand(BTServerCommand.MPD_CMD_PLAYLIST_ADD, url.toString());
        refresh();
    }

    /**
     * Clears playlist content.
     *
     * @throws NoBluetoothServerConnectionException if an error occur while contacting server.
     */
    public void clear() throws NoBluetoothServerConnectionException {
        btConnection.sendCommand(BTServerCommand.MPD_CMD_PLAYLIST_CLEAR);
        list.clear();
    }

    /**
     * Retrieves all songs as an <code>List</code> of <code>Music</code>.
     *
     * @return all songs as an <code>List</code> of <code>Music</code>.
     * @see Music
     */
    public List<Music> getMusicList() {
        return this.list.getMusic();
    }

    /**
     * Retrieves music at position index in playlist. Operates on local copy of playlist, may not reflect server's current playlist.
     *
     * @param index position.
     * @return music at position index.
     */
    public Music getByIndex(int index) {
        return list.getByIndex(index);
    }

    /**
     * Load playlist file.
     *
     * @param file playlist filename without .m3u extension.
     * @throws NoBluetoothServerConnectionException if an error occur while contacting server.
     */
    public void load(String file) throws NoBluetoothServerConnectionException {
        btConnection.sendCommand(BTServerCommand.MPD_CMD_PLAYLIST_LOAD, file);
        refresh();
    }

    /**
     * Moves song at position <code>from</code> to position <code>to</code>.
     *
     * @param from current position of the song to be moved.
     * @param to   target position of the song to be moved.
     * @throws NoBluetoothServerConnectionException if an error occur while contacting server.
     * @see #move(int, int)
     */
    public void moveByPosition(int from, int to) throws NoBluetoothServerConnectionException {
        btConnection.sendCommand(BTServerCommand.MPD_CMD_PLAYLIST_MOVE, Integer.toString(from), Integer.toString(to));
        refresh();
    }

    /**
     * Moves song with specified id to position <code>to</code>.
     *
     * @param songId Id of the song to be moved.
     * @param to     target position of the song to be moved.
     * @throws NoBluetoothServerConnectionException if an error occur while contacting server.
     */
    public void move(int songId, int to) throws NoBluetoothServerConnectionException {
        btConnection.sendCommand(BTServerCommand.MPD_CMD_PLAYLIST_MOVE_ID, Integer.toString(songId), Integer.toString(to));
        refresh();
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
     */
    protected int refresh() {
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
        return lastPlaylistVersion;
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
        List<Music> newPlaylist = new ArrayList<Music>(newLength + 1);

        newPlaylist.addAll(this.list.subList(0, newLength < oldLength ? newLength : oldLength));

        for (int i = newLength - oldLength; i > 0; i--)
            newPlaylist.add(null);

        for (Music song : changes) {
            if (newPlaylist.size() > song.getPos() && song.getPos() > -1) {
                newPlaylist.set(song.getPos(), song);
            }
        }

        this.list.clear();
        this.list.addAll(newPlaylist);
        if (updateListener != null) {
            updateListener.playlistUpdated();
        } else {
            Log.i(TAG, "It was null in btmd");
        }
    }

    /**
     * Remove playlist entry at position index.
     *
     * @param position position of the entry to be removed.
     * @throws NoBluetoothServerConnectionException if an error occur while contacting server.
     */
    public void removeByIndex(int position) throws NoBluetoothServerConnectionException {
        btConnection.sendCommand(BTServerCommand.MPD_CMD_PLAYLIST_REMOVE, Integer.toString(position));
        list.removeByIndex(position);
    }

    /**
     * Removes entries from playlist.
     *
     * @param songs entries positions.
     * @throws NoBluetoothServerConnectionException if an error occur while contacting server.
     * @see #removeById(int[])
     */
    public void removeByIndex(int[] songs) throws NoBluetoothServerConnectionException {
        java.util.Arrays.sort(songs);

        for (int i = songs.length - 1; i >= 0; i--)
            btConnection.queueCommand(BTServerCommand.MPD_CMD_PLAYLIST_REMOVE, Integer.toString(songs[i]));
        btConnection.sendCommandQueue();

        for (int i = songs.length - 1; i >= 0; i--)
            list.removeByIndex(songs[i]);
    }

    /**
     * Remove playlist entry with ID songId
     *
     * @param songId id of the entry to be removed.
     * @throws NoBluetoothServerConnectionException if an error occur while contacting server.
     */
    public void removeById(int songId) throws NoBluetoothServerConnectionException {
        btConnection.sendCommand(BTServerCommand.MPD_CMD_PLAYLIST_REMOVE_ID, Integer.toString(songId));
        list.removeById(songId);
    }

    /**
     * Removes entries from playlist.
     *
     * @param songIds entries IDs.
     * @throws NoBluetoothServerConnectionException if an error occur while contacting server.
     */
    public void removeById(int[] songIds) throws NoBluetoothServerConnectionException {
        for (int id : songIds)
            btConnection.queueCommand(BTServerCommand.MPD_CMD_PLAYLIST_REMOVE_ID, Integer.toString(id));
        btConnection.sendCommandQueue();

        for (int id : songIds)
            list.removeById(id);
    }

    /**
     * Removes album of given ID from playlist.
     *
     * @param songId entries positions.
     * @throws NoBluetoothServerConnectionException if an error occur while contacting server.
     * @see #removeById(int[])
     */
    public void removeAlbumById(int songId) throws NoBluetoothServerConnectionException {
        List<Music> songs = getMusicList();
        // Better way to get artist of given songId?
        String artist = "";
        String album = "";
        boolean usingAlbumArtist = true;
        for (Music song : songs)
            if (song.getSongId() == songId) {
                artist = song.getAlbumArtist();
                if (artist == null || artist.equals("")) {
                    usingAlbumArtist = false;
                    artist = song.getArtist();
                }
                album = song.getAlbum();
                break;
            }
        if (artist == null || album == null)
            return;
        if (DEBUG)
            Log.d("MPD", "Remove album " + album + " of " + artist);

        // Have artist & album, remove matching:
        int num = 0;
        for (Music song : songs)
            if (album.equals(song.getAlbum())) {
                if (usingAlbumArtist && artist.equals(song.getAlbumArtist()) ||
                        !usingAlbumArtist && artist.equals(song.getArtist())) {
                    int id = song.getSongId();
                    btConnection.queueCommand(BTServerCommand.MPD_CMD_PLAYLIST_REMOVE_ID, Integer.toString(id));
                    list.removeById(id);
                    num++;
                }
            }
        if (DEBUG)
            Log.d("MPD", "Removed " + num + " songs");
        btConnection.sendCommandQueue();
    }

    /**
     * Removes playlist file.
     *
     * @param file playlist filename without .m3u extension.
     * @throws NoBluetoothServerConnectionException if an error occur while contacting server.
     */
    public void removePlaylist(String file) throws NoBluetoothServerConnectionException {
        btConnection.sendCommand(BTServerCommand.MPD_CMD_PLAYLIST_DELETE, file);
    }

    /**
     * Save playlist file.
     *
     * @param file playlist filename without .m3u extension.
     * @throws NoBluetoothServerConnectionException if an error occur while contacting server.
     */
    public void savePlaylist(String file) throws NoBluetoothServerConnectionException {
        // If the playlist already exists, save will fail. So, just remove it first!
        btConnection.sendCommand(BTServerCommand.MPD_CMD_PLAYLIST_DELETE, file); // TODO figure out how to do this with bt
//        try {
//        } catch (MPDServerException e) {
//            // Guess the file did not exist???
//        }
        btConnection.sendCommand(BTServerCommand.MPD_CMD_PLAYLIST_SAVE, file);
    }

    /**
     * Shuffles playlist content.
     *
     * @throws NoBluetoothServerConnectionException if an error occur while contacting server.
     */
    public void shuffle() throws NoBluetoothServerConnectionException {
        btConnection.sendCommand(BTServerCommand.MPD_CMD_PLAYLIST_SHUFFLE);
    }

    /**
     * Retrieves playlist size. Operates on local copy of playlist, may not reflect server's current playlist. You may call refresh() before
     * calling size().
     *
     * @return playlist size.
     */
    public int size() {
        return list.size();
    }

    /**
     * Swap positions of song1 and song2.
     *
     * @param song1 position of song1 in playlist.
     * @param song2 position of song2 in playlist
     * @throws NoBluetoothServerConnectionException if an error occur while contacting server.
     * @see #swap(int, int)
     */
    public void swapByPosition(int song1, int song2) throws NoBluetoothServerConnectionException {
        btConnection.sendCommand(BTServerCommand.MPD_CMD_PLAYLIST_SWAP, Integer.toString(song1), Integer.toString(song2));
        this.refresh();
    }

    /**
     * Swap positions of song1 and song2.
     *
     * @param song1Id id of song1 in playlist.
     * @param song2Id id of song2 in playlist.
     * @throws NoBluetoothServerConnectionException if an error occur while contacting server.
     */
    public void swap(int song1Id, int song2Id) throws NoBluetoothServerConnectionException {
        btConnection.sendCommand(BTServerCommand.MPD_CMD_PLAYLIST_SWAP_ID, Integer.toString(song1Id), Integer.toString(song2Id));
        this.refresh();
    }
}

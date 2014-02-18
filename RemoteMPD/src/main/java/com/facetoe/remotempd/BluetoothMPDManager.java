package com.facetoe.remotempd;

import android.util.Log;
import com.facetoe.remotempd.listeners.ConnectionListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.a0z.mpd.AbstractCommand;
import org.a0z.mpd.AbstractMPDPlaylist;
import org.a0z.mpd.FilesystemTreeEntry;
import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.event.TrackPositionListener;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

class BluetoothMPDManager extends AbstractMPDManager implements
        ConnectionListener {

    private static final String TAG = RMPDApplication.APP_PREFIX + "BluetoothMPDManager";
    private RMPDApplication app = RMPDApplication.getInstance();

    private final BluetoothConnection btConnection;
    private final BluetoothMPDStatusMonitor btMonitor;
    private BTMPDPlaylist playlist;
    ArrayBlockingQueue<MPDResponse> syncedCommandQueue = new ArrayBlockingQueue<MPDResponse>(10);
    private Gson gson = new Gson();

    public BluetoothMPDManager() {
        btMonitor = new BluetoothMPDStatusMonitor();
        btConnection = new BluetoothConnection(btMonitor, syncedCommandQueue, this);
        playlist = new BTMPDPlaylist(btConnection);
        btMonitor.setPlaylistUpdateListener(playlist);
        btMonitor.addStatusChangeListener(playlist);
    }

    @Override
    protected void connectInternal() {
        if (!btConnection.isConnected()) {
            btConnection.connect();
        }
    }

    @Override
    public boolean isConnected() {
        return btConnection.isConnected();
    }

    @Override
    public void disconnect() {
        btConnection.disconnect();
        Log.i(TAG, "Disconnected from bluetooth: " + !btConnection.isConnected());
    }

    @Override
    public void connectionSucceeded(String message) {
        Log.i(TAG, "Connection succeeded in BluetoothConnection: " + message);
        app.notifyEvent(RMPDApplication.Event.CONNECTION_SUCCEEDED);
    }

    @Override
    public void connectionFailed(String message) {
        Log.w(TAG, "Connection failed in BluetoothConnection: " + message);
        app.notifyEvent(RMPDApplication.Event.CONNECTION_FAILED, message);
    }

    @Override
    public void play() {
        sendCommand(new BTServerCommand(BTServerCommand.MPD_CMD_PLAY));
    }

    @Override
    public void playID(int id) {
        sendCommand(new BTServerCommand(BTServerCommand.MPD_CMD_PLAY_ID, Integer.toString(id)));
    }

    @Override
    public void stop() {
        sendCommand(new BTServerCommand(BTServerCommand.MPD_CMD_STOP));
    }

    @Override
    public void pause() {
        sendCommand(new BTServerCommand(BTServerCommand.MPD_CMD_PAUSE));
    }

    @Override
    public void next() {
        sendCommand(new BTServerCommand(BTServerCommand.MPD_CMD_NEXT));
    }

    @Override
    public void prev() {
        sendCommand(new BTServerCommand(BTServerCommand.MPD_CMD_PREV));
    }

    @Override
    public void setVolume(int newVolume) {
        sendCommand(new BTServerCommand(BTServerCommand.MPD_CMD_VOLUME, Integer.toString(newVolume)));
    }

    @Override
    public List<String> listAlbums() {
        BTServerCommand command = new BTServerCommand(BTServerCommand.MPD_CMD_LIST_TAG, BTServerCommand.MPD_TAG_ALBUM);
        command.setSynchronous(true);
        final Future<MPDResponse> responseFuture = btConnection.syncedWriteRead(command);
        return getResultFromFuture(responseFuture);
    }

    @Override
    public List<String> listArtists() {
        BTServerCommand command = new BTServerCommand(BTServerCommand.MPD_CMD_LIST_TAG, BTServerCommand.MPD_TAG_ARTIST);
        command.setSynchronous(true);
        final Future<MPDResponse> responseFuture = btConnection.syncedWriteRead(command);
        return getResultFromFuture(responseFuture);
    }

    private List<String> getResultFromFuture(Future<MPDResponse> responseFuture) {
        try {
            MPDResponse response = responseFuture.get();
            return extractStringList(response);
        } catch (InterruptedException e) {
            Log.e(TAG, "Fuck", e);
        } catch (ExecutionException e) {
            Log.e(TAG, "Fuck", e);
        }
        return Collections.emptyList();
    }

    private List<String> extractStringList(MPDResponse response) {
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(response.getObjectJSON(0), listType);
    }

    @Override
    public void sendCommand(AbstractCommand command) {
        try {
            // Need to convert it here to make life easier on the other end.
            BTServerCommand serverCommand = new BTServerCommand(command.getCommand(), command.getArgs());
            btConnection.sendCommand(serverCommand);
            Log.i(TAG, "Sent command: " + command);
        } catch (Exception e) {
            connectionFailed(e.getMessage());
        }
    }

    @Override
    public AbstractMPDPlaylist getPlaylist() {
        return playlist;
    }

    @Override
    public void addStatusChangeListener(StatusChangeListener listener) {
        btMonitor.addStatusChangeListener(listener);
    }

    @Override
    public void removeStatusChangeListener(StatusChangeListener listener) {
        btMonitor.removeStatusChangeListener(listener);
    }

    @Override
    public void addTrackPositionListener(TrackPositionListener listener) {
        btMonitor.addTrackPositionListener(listener);
    }

    @Override
    public void removeTrackPositionListener(TrackPositionListener listener) {
        btMonitor.removeTrackPositionListener(listener);
    }
}
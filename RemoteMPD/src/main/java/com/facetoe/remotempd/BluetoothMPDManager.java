package com.facetoe.remotempd;

import android.util.Log;
import com.facetoe.remotempd.listeners.ConnectionListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.a0z.mpd.AbstractCommand;
import org.a0z.mpd.AbstractMPDPlaylist;
import org.a0z.mpd.FilesystemTreeEntry;
import org.a0z.mpd.MPDPlaylist;
import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.event.TrackPositionListener;

import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

class BluetoothMPDManager extends AbstractMPDManager implements
        ConnectionListener {

    private static final String TAG = RMPDApplication.APP_PREFIX + "BluetoothMPDManager";
    private RMPDApplication app = RMPDApplication.getInstance();

    private final BluetoothConnection btConnection;
    private final BluetoothMPDStatusMonitor btMonitor;
    private BTMPDPlaylist playlist;
    LinkedBlockingQueue<MPDResponse> syncedCommandQueue = new LinkedBlockingQueue<MPDResponse>();
    private Gson gson = new Gson();
    private long TIMEOUT = 2;

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
    List<String[]> sendCommandQueueSeparated() {
        throw new IllegalStateException("Not implemented yet!");
    }

    static List<String[]> separatedQueueResults(List<String> lines) {
        List<String[]> result = new ArrayList<String[]>();
        ArrayList<String> lineCache = new ArrayList<String>();

        for (String line : lines) {
            if (line.equals(BTServerCommand.MPD_CMD_BULK_SEP)) { // new part
                if (lineCache.size() != 0) {
                    result.add((String[]) lineCache.toArray(new String[0]));
                    lineCache.clear();
                }
            } else
                lineCache.add(line);
        }
        if (lineCache.size() != 0) {
            result.add((String[]) lineCache.toArray(new String[0]));
        }
        return result;
    }

    public List<String> sendCommand(AbstractCommand command) {
        return sendCommand(command.getCommand(), command.getArgs());
    }

    @Override
    List<String> sendCommand(String command) {
        BTServerCommand serverCommand = new BTServerCommand(command);
        serverCommand.setSynchronous(true);
        final Future<MPDResponse> responseFuture = btConnection.syncedWriteRead(serverCommand);
        return getResultFromFuture(responseFuture);
    }

    @Override
    List<String> sendCommand(String command, String... args) {
        BTServerCommand serverCommand = new BTServerCommand(command, args);
        serverCommand.setSynchronous(true);
        final Future<MPDResponse> responseFuture = btConnection.syncedWriteRead(serverCommand);
        return getResultFromFuture(responseFuture);
    }

    private List<String> getResultFromFuture(Future<MPDResponse> responseFuture) {
        try {
            MPDResponse response = responseFuture.get(TIMEOUT,TimeUnit.SECONDS );
            return extractStringList(response);
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted in getResultFromFuture()", e);
        } catch (ExecutionException e) {
            Log.e(TAG, "Exeption in getResultFromFuture()");
        } catch (TimeoutException e) {
            Log.e(TAG, "getResultFromFuture() timed out");
        }
        return Collections.emptyList();
    }

    private List<String> extractStringList(MPDResponse response) {
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(response.getObjectJSON(0), listType);
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
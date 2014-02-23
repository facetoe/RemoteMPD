package org.a0z.mpd;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.a0z.mpd.exception.NoBluetoothServerConnectionException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * dmixUnmodified
 * Created by facetoe on 20/02/14.
 */
public class BluetoothMPDConnection extends AbstractMPDConnection {

    private static final String TAG = "BluetoothMPDConnection";
    BluetoothDevice device;
    BluetoothConnection btConnection;
    Gson gson = new Gson();

    private static final long TIMEOUT = 10;

    public BluetoothMPDConnection(BluetoothDevice device) throws NoBluetoothServerConnectionException {
        this.device = device;
        btConnection = new BluetoothConnection(device);
        connect();
    }

    @Override
    int[] connect() throws NoBluetoothServerConnectionException {
        btConnection.connect();
        return new int[0]; // TODO return the version somehow.
    }

    @Override
    void disconnect() throws NoBluetoothServerConnectionException {
        btConnection.disconnect();
    }

    @Override
    public boolean isConnected() {
        return btConnection.isConnected();
    }

    @Override
    int[] getMpdVersion() {
        return new int[0]; //TODO What do?
    }

    @Override
    public List<String> sendCommand(AbstractCommand command) throws NoBluetoothServerConnectionException {
        return sendCommand(command.getCommand(), command.getArgs());
    }

    @Override
    public void queueCommand(String command, String... args) {
        queueCommand(new BTServerCommand(command, args));
    }

    @Override
    public void queueCommand(AbstractCommand command) {
        commandQueue.add(command);
    }

    @Override
    public List<String[]> sendCommandQueueSeparated() throws NoBluetoothServerConnectionException {
        return separatedQueueResults(sendCommandQueue(true));
    }

    @Override
    public List<String> sendCommandQueue() throws NoBluetoothServerConnectionException {
        return sendCommandQueue(false);
    }

    @Override
    List<String> sendCommandQueue(boolean withSeparator) throws NoBluetoothServerConnectionException {
        String commandstr;

        if (withSeparator) {
            commandstr = BTServerCommand.MPD_CMD_START_BULK_OK + "\n";
        } else {
            commandstr = BTServerCommand.MPD_CMD_START_BULK + "\n";
        }

        // Build the command using a temporary array as it is possible for a ConcurrentModificationException
        // to be thrown if the commandQueue is modified while it is being iterated over.
        ArrayList<AbstractCommand> tmpList = new ArrayList<AbstractCommand>(commandQueue);
        for (AbstractCommand command : tmpList) {
            commandstr += command.toString();
        }

        commandstr += BTServerCommand.MPD_CMD_END_BULK + "\n";
        commandQueue = new ArrayList<AbstractCommand>();
        return sendCommand(new BTServerCommand(commandstr));
    }

    @Override
    public List<String> sendRawCommand(AbstractCommand command) throws NoBluetoothServerConnectionException {
        return sendCommand(command);
    }

    @Override
    public List<String> sendCommand(String command, String... args) throws NoBluetoothServerConnectionException {
        BTServerCommand serverCommand = new BTServerCommand(command, args);
        serverCommand.setSynchronous(true);
        final Future<MPDResponse> responseFuture = btConnection.syncedWriteRead(serverCommand);
        return getResultFromFuture(responseFuture);
    }

    private List<String> getResultFromFuture(Future<MPDResponse> responseFuture) {
        try {
            // This call blocks until we get a response from the bluetooth server and
            // it is added to the queue, or it times out.
            MPDResponse response = responseFuture.get(TIMEOUT, TimeUnit.SECONDS);
            return extractStringList(response);
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted in getResultFromFuture()", e);
        } catch (ExecutionException e) {
            Log.e(TAG, "Exeption in getResultFromFuture()", e);
        } catch (TimeoutException e) {
            Log.e(TAG, "getResultFromFuture() timed out", e);
        }
        return Collections.emptyList();
    }

    public List<String> waitForChanges() {
        try {
            // This call blocks until the bluetooth server sends an update
            // and it is added to the queue.
            MPDResponse response = btConnection.mpdIdleUpdateQueue.take();
            return extractStringList(response);
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted", e);
        }
        return Collections.emptyList();
    }

    private List<String> extractStringList(MPDResponse response) {
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        return gson.fromJson(response.getObjectJSON(0), listType);
    }
}

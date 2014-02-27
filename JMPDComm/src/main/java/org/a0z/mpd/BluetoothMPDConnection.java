package org.a0z.mpd;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.a0z.mpd.exception.BluetoothServerException;
import org.a0z.mpd.exception.MPDServerException;

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
    int[] version;

    private static final long TIMEOUT = 10;

    public BluetoothMPDConnection(BluetoothDevice device) throws MPDServerException {
        this.device = device;
        btConnection = new BluetoothConnection(device);
    }

    @Override
    int[] connect() throws MPDServerException {
        btConnection.connect();
        List<String> status = sendCommand(new BTServerCommand(BTServerCommand.SERVER_CAN_PROCEED));
        return checkStatus(status);
    }

    // The server should respond with "OK <mpd version>". If it doesn't then something went
    // wrong and we should throw an exception.
    private int[] checkStatus(List<String> response) throws BluetoothServerException {
        if (response.size() > 0) {
            String status = response.get(0);
            if (status.startsWith("OK")) {
                version = getMPDVersionFromStatus(status);
                Log.i(TAG, "Status: " + status);
                return version;
            }
        }
        throw new BluetoothServerException("Error connecting to MPD");
    }

    private static int[] getMPDVersionFromStatus(String status) {
        String[] digits = status.substring("OK ".length(), status.length()).split("\\.");
        int[] integers = new int[digits.length];
        for (int i = 0; i < digits.length; i++) {
            integers[i] = Integer.valueOf(digits[i]);
        }
        return integers;
    }

    @Override
    void disconnect() throws MPDServerException {
        btConnection.disconnect();
    }

    @Override
    public boolean isConnected() {
        return btConnection.isConnected();
    }

    @Override
    int[] getMpdVersion() {
        return version;
    }

    @Override
    public List<String> sendCommand(AbstractCommand command) throws MPDServerException {
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
    public List<String[]> sendCommandQueueSeparated() throws MPDServerException {
        return separatedQueueResults(sendCommandQueue(true));
    }

    @Override
    public List<String> sendCommandQueue() throws MPDServerException {
        return sendCommandQueue(false);
    }

    @Override
    List<String> sendCommandQueue(boolean withSeparator) throws MPDServerException {
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
    public List<String> sendRawCommand(AbstractCommand command) throws MPDServerException {
        return sendCommand(command);
    }

    @Override
    public List<String> sendCommand(String command, String... args) throws MPDServerException {
        BTServerCommand serverCommand = new BTServerCommand(command, args);
        serverCommand.setSynchronous(true);
        final Future<MPDResponse> responseFuture = btConnection.syncedWriteRead(serverCommand);
        return getResultFromFuture(responseFuture);
    }

    private List<String> getResultFromFuture(Future<MPDResponse> responseFuture) throws MPDServerException {
        try {
            // This call blocks until we get a response from the bluetooth server and
            // it is added to the queue, or it times out.
            MPDResponse response = responseFuture.get(TIMEOUT, TimeUnit.SECONDS);
            checkForError(response);
            return extractStringList(response);
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted in getResultFromFuture()", e);
        } catch (ExecutionException e) {
            Log.e(TAG, "Exeption in getResultFromFuture()", e);
        } catch (TimeoutException e) {
            Log.e(TAG, "Timeout in getResultFromFuture()");
        }
        return Collections.emptyList();
    }

    public List<String> waitForChanges() throws MPDServerException {
        try {
            // This call blocks until the bluetooth server sends an update
            // and it is added to the queue.
            MPDResponse response = btConnection.mpdIdleUpdateQueue.take();
            checkForError(response);
            return extractStringList(response);
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted", e);
        }
        return Collections.emptyList();
    }

    private void checkForError(MPDResponse response) throws MPDServerException {
        if (response.getResponseType() == MPDResponse.EVENT_ERROR) {
            Log.e(TAG, "Received error, throwing exception!");

            if(btConnection != null) {
                btConnection.disconnect();
            }

            String errorMsg = "";
            if (response.getNumObjects() > 0) {
                errorMsg = gson.toJson(response.getObjectJSON(0), String.class);
            }
            throw new MPDServerException(errorMsg);
        }
    }

    private List<String> extractStringList(MPDResponse response) {
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        return gson.fromJson(response.getObjectJSON(0), listType);
    }
}

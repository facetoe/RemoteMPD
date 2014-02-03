package com.facetoe.remotempd;

import android.util.Log;
import org.a0z.mpd.MPDCommand;
import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.event.TrackPositionListener;

/**
 * Created by facetoe on 5/01/14.
 */

interface MPDConnectionController {
    void connect();
    boolean isConnected();
    void disconnect();
}
public abstract class AbstractMPDManager implements MPDPlayerController, MPDConnectionController {
    private static final String TAG = RemoteMPDApplication.APP_PREFIX + "AbstractMPDManager";

    RemoteMPDApplication app = RemoteMPDApplication.getInstance();
    protected static final int MAX_COMMAND_RETRY_ATTEMPTS = 3;
    protected int retryAttempts;
    protected MPDCommand lastCommand;

    abstract public void addStatusChangeListener(StatusChangeListener listener);
    abstract public void removeStatusChangeListener(StatusChangeListener listener);
    abstract public void addTrackPositionListener(TrackPositionListener listener);
    abstract public void removeTrackPositionListener(TrackPositionListener listener);

    /**
     *
     * @param command
     */
    abstract void sendCommand(MPDCommand command);

    @Override
    public void play() {
        sendCommand(new MPDCommand(MPDCommand.MPD_CMD_PLAY));
    }

    @Override
    public void playID(int id) {
        sendCommand(new MPDCommand(MPDCommand.MPD_CMD_PLAY_ID, Integer.toString(id)));
    }

    @Override
    public void stop() {
        sendCommand(new MPDCommand(MPDCommand.MPD_CMD_STOP));
    }

    @Override
    public void pause() {
        sendCommand(new MPDCommand(MPDCommand.MPD_CMD_PAUSE));
    }

    @Override
    public void next() {
        sendCommand(new MPDCommand(MPDCommand.MPD_CMD_NEXT));
    }

    @Override
    public void prev() {
        sendCommand(new MPDCommand(MPDCommand.MPD_CMD_PREV));
    }

    @Override
    public void setVolume(int newVolume) {
        if (newVolume < 0 || newVolume > 100) return;
        MPDCommand command = new MPDCommand(
                MPDCommand.MPD_CMD_VOLUME,
                Integer.toString(newVolume));
        sendCommand(command);
    }

    void attemptReconnect() {
        Log.w(TAG, "Attempting reconnect: " + retryAttempts);
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < MAX_COMMAND_RETRY_ATTEMPTS; i++) {
                    retryAttempts++;
                    connect();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "Reconnect interrupted");
                    }

                    if (isConnected()) {
                        Log.d(TAG, "Reconnect succeeded");
                        sendCommand(lastCommand);
                        return;
                    } else {
                        Log.d(TAG, "Reconnect failed");
                    }
                }
                app.connectionFailed(buildErrorMessage());
            }
        }).start();
    }

    private String buildErrorMessage() {
        RemoteMPDSettings settings = app.getSettings();
        String errMsg;
        if(settings.isBluetooth()) {
            if(settings.getLastDevice().isEmpty()) {
                errMsg = "No Bluetooth device selected!";
            } else {
                errMsg = "Failed to connect to Bluetooth server at: "
                        + settings.getLastDevice()
                        + ". Is it running?";
            }
        } else {
            if(settings.getHost().isEmpty()) {
                errMsg = "No host IP defined!";
            } else {
                errMsg = "Failed to connect to " + settings.getHost()
                        + " on port " + settings.getPort();
            }
        }
        return errMsg;
    }

    void handleError(Exception e) {
        RemoteMPDApplication.getInstance().connectionFailed(e.getMessage());
    }

}
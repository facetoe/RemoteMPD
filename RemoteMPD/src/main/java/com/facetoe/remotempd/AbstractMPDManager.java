package com.facetoe.remotempd;

import org.a0z.mpd.AbstractCommand;
import org.a0z.mpd.MPDCommand;
import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.event.TrackPositionListener;

/**
 * Created by facetoe on 5/01/14.
 */

public abstract class AbstractMPDManager implements MPDPlayerController {
    private final RMPDApplication app = RMPDApplication.getInstance();

    abstract public void addStatusChangeListener(StatusChangeListener listener);
    abstract public void removeStatusChangeListener(StatusChangeListener listener);
    abstract public void addTrackPositionListener(TrackPositionListener listener);
    abstract public void removeTrackPositionListener(TrackPositionListener listener);

    /**
     *
     * @param command
     */
    abstract void sendCommand(AbstractCommand command);

    abstract protected void connectInternal();
    abstract public boolean isConnected();
    abstract public void disconnect();

    public void connect() {
        connectInternal();
        app.notifyEvent(RMPDApplication.Event.CONNECTING);
    }

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
}
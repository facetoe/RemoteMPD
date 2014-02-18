package com.facetoe.remotempd;

import org.a0z.mpd.AbstractCommand;
import org.a0z.mpd.AbstractMPDPlaylist;
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

    abstract public AbstractMPDPlaylist getPlaylist();

    public void connect() {
        connectInternal();
        app.notifyEvent(RMPDApplication.Event.CONNECTING);
    }
}
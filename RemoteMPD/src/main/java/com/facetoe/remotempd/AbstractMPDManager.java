package com.facetoe.remotempd;

import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.event.TrackPositionListener;

/**
 * Created by facetoe on 5/01/14.
 */
public abstract class AbstractMPDManager implements MPDPlayerController {
    abstract public void addStatusChangeListener(StatusChangeListener listener);
    abstract public void removeStatusChangeListener(StatusChangeListener listener);
    abstract public void addTrackPositionListener(TrackPositionListener listener);
    abstract public void removeTrackPositionListener(TrackPositionListener listener);
    abstract public void connect();
    abstract public boolean isConnected();
    abstract public void restart();
    abstract public void disconnect();
}
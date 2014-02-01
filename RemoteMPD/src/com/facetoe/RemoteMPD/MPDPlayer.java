package com.facetoe.RemoteMPD;

import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.event.TrackPositionListener;

public interface MPDPlayer {

    void play();

    void playID(int id);

    void stop(); // TODO remove this?

    void pause();

    void next();

    void prev();

    void setVolume(int newVolume);
}
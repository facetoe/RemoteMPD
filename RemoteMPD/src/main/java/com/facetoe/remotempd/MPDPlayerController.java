package com.facetoe.remotempd;

import org.a0z.mpd.MPDCommand;

public interface MPDPlayerController {

    void play();

    void playID(int id);

    void stop(); // TODO remove this?

    void pause();

    void next();

    void prev();

    void setVolume(int newVolume);
}
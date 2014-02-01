package com.facetoe.remotempd;

public interface MPDPlayerController {

    void play();

    void playID(int id);

    void stop(); // TODO remove this?

    void pause();

    void next();

    void prev();

    void setVolume(int newVolume);
}
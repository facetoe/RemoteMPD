package com.facetoe.RemoteMPD;

interface MPDPlayerController {

    void start();

    void play();

    void stop(); // TODO remove this?

    void pause();

    void next();

    void prev();

    void setVolume(int newVolume);
}
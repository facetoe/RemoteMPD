package com.facetoe.RemoteMPD;

interface PlayerController {
    void play();

    void stop(); // TODO remove this?

    void pause();

    void next();

    void prev();

    void setVolume(int newVolume);
}
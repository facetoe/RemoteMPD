package com.facetoe.RemoteMPD;

interface MPDPlayerController {

    void start();

    void restart();

    void disconnect();

    void play();

    void playID(int id);

    void stop(); // TODO remove this?

    void pause();

    void next();

    void prev();

    void setVolume(int newVolume);
}
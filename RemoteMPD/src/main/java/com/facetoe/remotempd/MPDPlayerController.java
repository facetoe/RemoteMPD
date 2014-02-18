package com.facetoe.remotempd;

import org.a0z.mpd.FilesystemTreeEntry;
import org.a0z.mpd.MPDCommand;

import java.util.List;

public interface MPDPlayerController {

    void play();

    void playID(int id);

    void stop(); // TODO remove this?

    void pause();

    void next();

    void prev();

    void setVolume(int newVolume);

    List<String> listAlbums();

    List<String> listArtists();
}
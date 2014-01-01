package com.facetoe.RemoteMPD;

import com.google.gson.Gson;

import java.io.Serializable;
public class MPDResponse implements Serializable {
    private static final String TAG = MPDResponse.class.getSimpleName();

    public static final int PLAYER_STOPPED = 0;
    public static final int PLAYER_STARTED = 1;
    public static final int PLAYER_PAUSED = 2;
    public static final int PLAYER_UNPAUSED = 3;
    public static final int PLAYER_RANDOM_CHANGED = 4;
    public static final int PLAYER_REPEAT_CHANGED = 5;
    public static final int PLAYER_SINGLE_CHANGED = 6;
    public static final int PLAYER_VOLUME_CHANGED = 7;
    public static final int PLAYER_MUTE_CHANGED = 8;

    public static final int PLAYER_UPDATE_CURRENTSONG = 9;
    public static final int PLAYER_UPDATE_TRACK_POSITION = 10;
    public static final int PLAYER_UPDATE_PLAYLIST = 11;

    private int responseType;
    private String objectJSON;

    public MPDResponse(int responseType, Object obj) {
        this.responseType = responseType;
        this.objectJSON = new Gson().toJson(obj);
    }

    public String getObjectJSON() {
        return objectJSON;
    }
    public int getResponseType() {
        return responseType;
    }
}
package com.facetoe.RemoteMPD;

import android.app.Application;
import android.util.Log;
import org.a0z.mpdlocal.Music;

import java.util.List;

/**
 * Created by facetoe on 31/12/13.
 */
public class RemoteMPDApplication extends Application {
    String APP_TAG = "RemoteMPDApplication";

    private static RemoteMPDApplication instance;
    List<Music> songList;

    public static RemoteMPDApplication getInstance() {
        checkInstance();
        return instance;
    }

    public List<Music> getSongList() {
        checkInstance();
        return songList;
    }

    public void setSongList(List<Music> songList) {
        this.songList = songList;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(APP_TAG, "Initializing application..");
        instance = this;
    }



    private static void checkInstance() {
        if (instance == null)
            throw new IllegalStateException("Application not created yet!");
    }
}

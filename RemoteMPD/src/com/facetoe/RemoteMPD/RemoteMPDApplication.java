package com.facetoe.RemoteMPD;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;
import com.facetoe.RemoteMPD.helpers.MPDAsyncHelper;
import org.a0z.mpd.Music;

import java.util.List;

/**
 * Created by facetoe on 31/12/13.
 */
public class RemoteMPDApplication extends Application {
    public final static String APP_TAG = "RemoteMPDApplication";
    private static final String PREFERENCES = "preferences";

    public static boolean isBluetooth = false;

    private static RemoteMPDApplication instance;
    List<Music> songList;
    SharedPreferences sharedPreferences;
    public MPDAsyncHelper asyncHelper;
    private AbstractMPDManager mpdManager;

    public static RemoteMPDApplication getInstance() {
        checkInstance();
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(APP_TAG, "Initializing application..");
        asyncHelper = new MPDAsyncHelper();
        asyncHelper.connect();
        instance = this;
    }



    public List<Music> getSongList() {
        checkInstance();
        return songList;
    }

    public void setSongList(List<Music> songList) {
        this.songList = songList;
    }

    public SharedPreferences getSharedPreferences() {
        checkInstance();
        if(sharedPreferences == null)
            sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        return sharedPreferences;
    }

    // TODO fix this to select the right manager based on user preference
    public AbstractMPDManager getMpdManager() {
        if(isBluetooth && mpdManager == null)
            mpdManager = new BluetoothMPDManager();
        else if(!isBluetooth && mpdManager == null)
            mpdManager = new WifiMPDManager();

        return mpdManager;
    }

    public void setMpdManager(AbstractMPDManager mpdManager) {
        this.mpdManager = mpdManager;
    }

    private static void checkInstance() {
        if (instance == null)
            throw new IllegalStateException("Application not created yet!");
    }
}

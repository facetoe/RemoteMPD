package com.facetoe.RemoteMPD;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.facetoe.RemoteMPD.helpers.MPDAsyncHelper;
import org.a0z.mpd.Music;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by facetoe on 31/12/13.
 */
public class RemoteMPDApplication extends Application implements MPDAsyncHelper.ConnectionListener {
    public final static String APP_TAG = "RemoteMPDApplication";
    private static final String PREFERENCES = "preferences";

    public static boolean isBluetooth = false;
    private Activity currentActivity;
    private Collection<Object> connectionLocks = new LinkedList<Object>();
    AlertDialog ad;

    private static RemoteMPDApplication instance;
    List<Music> songList;
    SharedPreferences sharedPreferences;
    private AbstractMPDManager mpdManager;

    class DialogClickListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_NEUTRAL:
                    currentActivity.startActivityForResult(new Intent(currentActivity, SettingsActivity.class), 5);
                    break;
                case AlertDialog.BUTTON_NEGATIVE:
                    currentActivity.finish();
                    break;
                case AlertDialog.BUTTON_POSITIVE:
                    mpdManager.connect();
                    break;
            }
        }
    }

    public static RemoteMPDApplication getInstance() {
        checkInstance();
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        checkSettings();
        Log.i(APP_TAG, "Initializing application..");
        instance = this;
    }

    private void checkSettings() {
        RemoteMPDSettings settings = getMPDWifiSettings();
        if(settings.getHost().isEmpty()) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
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
        if (sharedPreferences == null)
            sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        return sharedPreferences;
    }

    public RemoteMPDSettings getMPDWifiSettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String host = preferences.getString("mpdWifiHost", "");
        String port = preferences.getString("mpdWifiPort", "6600");
        String password = preferences.getString("mpdWifiPassword", "");
        String lastDevice = "NONE";
        boolean isBluetooth = preferences.getString("mpdConnectionOptions", "NONE").equals("wifi");
        return new RemoteMPDSettings(host, port, password, lastDevice, isBluetooth);
    }

    public AbstractMPDManager getMpdManager() {
        if (isBluetooth)
            mpdManager = BluetoothMPDManager.getInstance();
        else
            mpdManager = WifiMPDManager.getInstance();

        return mpdManager;
    }

    private static void checkInstance() {
        if (instance == null)
            throw new IllegalStateException("Application not created yet!");
    }

    @Override
    public void connectionFailed(String message) {
        Log.e(APP_TAG, "Connection Failed: " + message);
    }

    @Override
    public void connectionSucceeded(String message) {
        Log.e(APP_TAG, "Connection succeeded: " + message);
        dismissAlertDialog();
    }

    private void dismissAlertDialog() {
        if (ad != null) {
            if (ad.isShowing()) {
                try {
                    ad.dismiss();
                } catch (IllegalArgumentException e) {
                    Log.e(APP_TAG, "Shit", e);
                }
            }
        }
    }

    public void setCurrentActivity(Activity currentActivity) {
        this.currentActivity = currentActivity;
        checkConnectionNeeded();
        connectionLocks.add(currentActivity);
    }

    public void unsetActivity() {
        connectionLocks.remove(currentActivity);
        checkConnectionNeeded();
        this.currentActivity = null;
    }

    private void checkConnectionNeeded() {
        Log.i(APP_TAG, "Checking connection");
        if (connectionLocks.size() > 0 && (currentActivity == null || !currentActivity.getClass().equals(SettingsActivity.class))) {
            if (!mpdManager.isConnected()) {
                mpdManager.connect();
            }
        } else {
            //disconnect();
        }
    }
}

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by facetoe on 31/12/13.
 */

public class RemoteMPDApplication extends Application implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    public final static String APP_TAG = "RemoteMPDApplication";
    public static final String PREFERENCES = "preferences";
    private String oldConnectionSetting;

    public static boolean isBluetooth = false;
    private Activity currentActivity;
    private Collection<Object> connectionLocks = new LinkedList<Object>();
    private ArrayList<MPDManagerChangeListener> mpdManagerChangeListeners = new ArrayList<MPDManagerChangeListener>();
    AlertDialog ad;

    private static RemoteMPDApplication instance;
    List<Music> songList;
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
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        checkSettings();
        Log.i(APP_TAG, "Initializing application..");
        instance = this;
    }

    private void checkSettings() {
        Log.i(APP_TAG, "Checking settings");
        RemoteMPDSettings settings = getRemoteMPDSettings();
        if (settings.isBluetooth()) {
            if (settings.getLastDevice().equals(DeviceListActivity.NO_DEVICE)) {
                Intent intent = new Intent(this, DeviceListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } else {
            if (settings.getHost().isEmpty()) {
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
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
        return getSharedPreferences(PREFERENCES, MODE_PRIVATE);
    }

    public RemoteMPDSettings getRemoteMPDSettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String host = preferences.getString("mpdWifiHost", "");
        String port = preferences.getString("mpdWifiPort", "6600");
        String password = preferences.getString("mpdWifiPassword", "");
        String lastDevice = getSharedPreferences().getString(BluetoothController.LAST_DEVICE_KEY, "NONE");

        boolean isBluetooth = preferences.getString("mpdConnectionSetting", "wifi").equals("bluetooth");
        return new RemoteMPDSettings(host, port, password, lastDevice, isBluetooth);
    }

    public AbstractMPDManager getMpdManager() {
        if (getRemoteMPDSettings().isBluetooth())
            mpdManager = new BluetoothMPDManager();
        else
            mpdManager = new WifiMPDManager();

        return mpdManager;
    }

    private static void checkInstance() {
        if (instance == null)
            throw new IllegalStateException("Application not created yet!");
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
        this.currentActivity = null;
    }

    private void checkConnectionNeeded() {
        Log.i(APP_TAG, "Checking connection");
        if (getRemoteMPDSettings().isBluetooth()) {
            if (mpdManager != null && !mpdManager.isConnected()) {
                mpdManager.connect();
            }
        } else if (connectionLocks.size() > 0 && (currentActivity == null || !currentActivity.getClass().equals(SettingsActivity.class))) {
            if (mpdManager != null && !mpdManager.isConnected()) {
                mpdManager.connect();
            }
        } else  {
            Log.e(APP_TAG, "else");
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        RemoteMPDSettings settings = getRemoteMPDSettings();
        Log.i(APP_TAG, "Preference: " + key + "changed.\n" + settings);
        fireMPDManagerChanged();
    }

    public void addMPDManagerChangeListener(MPDManagerChangeListener listener) {
        mpdManagerChangeListeners.add(listener);
    }

    public void removeMPDManagerChangeListener(MPDManagerChangeListener listener) {
        mpdManagerChangeListeners.remove(listener);
    }

    private void fireMPDManagerChanged() {
        for (MPDManagerChangeListener listener : mpdManagerChangeListeners) {
            listener.mpdManagerChanged();
        }
    }
}

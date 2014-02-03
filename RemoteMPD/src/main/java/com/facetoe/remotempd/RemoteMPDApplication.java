package com.facetoe.remotempd;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.facetoe.remotempd.helpers.DialogBuilderFactory;
import com.facetoe.remotempd.helpers.MPDAsyncHelper;
import com.facetoe.remotempd.helpers.SettingsHelper;

import java.util.ArrayList;

/**
 * Created by facetoe on 31/12/13.
 */


public class RemoteMPDApplication extends Application implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        MPDAsyncHelper.ConnectionListener {

    private static RemoteMPDApplication instance;
    public final static String APP_PREFIX = "RMPD-";
    private static final String TAG = APP_PREFIX + "RemoteMPDApplication";

    private SettingsHelper settingsHelper;
    private AlertDialog dialog;
    private Activity currentActivity;
    private AbstractMPDManager mpdManager;
    private final ArrayList<MPDManagerChangeListener> mpdManagerChangeListeners = new ArrayList<MPDManagerChangeListener>();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        settingsHelper = new SettingsHelper(this);
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    public static RemoteMPDApplication getInstance() {
        checkInstance();
        return instance;
    }

    public void registerCurrentActivity(Activity activity) {
        Log.d(TAG, "Registering: " + activity);
        checkInstance();
        currentActivity = activity;
        checkSettings();
        checkConnection();
    }

    private void checkSettings() {
        if (currentActivity instanceof SettingsActivity) {
            return;
        }

        if (settingsHelper.getSettings().isBluetooth() && !settingsHelper.hasBluetoothSettings()) {
            AlertDialog.Builder builder = DialogBuilderFactory.getNoBluetoothSettingsDialog(currentActivity);
            showDialog(builder);
        } else if (!settingsHelper.getSettings().isBluetooth() && !settingsHelper.hasWifiSettings()) {
            AlertDialog.Builder builder = DialogBuilderFactory.getNoWifiSettingsDialog(currentActivity);
            showDialog(builder);
        } else {
            Log.e(TAG, "I Don't know what dialog to show.");
        }
    }

    private void checkConnection() {
        if (mpdManager != null && !mpdManager.isConnected()) {
            mpdManager.connect();
        }
    }

    public void unregisterCurrentActivity() {
        checkInstance();
        Log.d(TAG, "Unregistering: " + currentActivity);
        currentActivity = null;
    }

    public void addMpdManagerChangeListener(MPDManagerChangeListener listener) {
        mpdManagerChangeListeners.add(listener);
    }

    public void removeMpdManagerChangeListener(MPDManagerChangeListener listener) {
        mpdManagerChangeListeners.remove(listener);
    }

    private void fireMPDManagerChanged() {
        for (MPDManagerChangeListener listener : mpdManagerChangeListeners) {
            listener.mpdManagerChanged();
        }
    }

    public AbstractMPDManager getMpdManager() {
        if (settingsHelper.getSettings().isBluetooth()) {
            if (mpdManager == null || mpdManager instanceof WifiMPDManager)
                mpdManager = new BluetoothMPDManager();
        } else {
            if (mpdManager == null || mpdManager instanceof BluetoothMPDManager)
                mpdManager = new WifiMPDManager();
        }
        return mpdManager;
    }

    public RemoteMPDSettings getSettings() {
        return settingsHelper.getSettings();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i(TAG, "Shared preferences changed: " + key);
        if (key.equals(getString(R.string.remoteMpdConnectionTypeKey))) {
            fireMPDManagerChanged();
        }
    }

    @Override
    public void connectionFailed(String message) {
        Log.i(TAG, "CurrentActivity: " + currentActivity);
        maybeShowConnectionFailedDialog(message);
    }

    private void maybeShowConnectionFailedDialog(String message) {
        if (currentActivity == null
                || currentActivity instanceof SettingsActivity
                || mpdManager.isConnected()
                || dialog != null && dialog.isShowing()) {
            return;
        }
        AlertDialog.Builder builder = DialogBuilderFactory.getConnectionFailedDialog(currentActivity, message);
        showDialog(builder);
    }

    private void showDialog(final AlertDialog.Builder builder) {
        currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog = builder.create();
                dialog.show();
            }
        });
    }


    @Override
    public void connectionSucceeded(String message) {
        Log.i(TAG, "Connection succeeded");
    }

    private static void checkInstance() {
        if (instance == null)
            throw new IllegalStateException("Application not created yet!");
    }
}

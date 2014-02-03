package com.facetoe.remotempd;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
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
    private Activity currentActivity;
    private AbstractMPDManager mpdManager;
    private final ArrayList<MPDManagerChangeListener> mpdManagerChangeListeners = new ArrayList<MPDManagerChangeListener>();

    private int MAX_RECONNECT_ATTEMPTS = 3;
    private int reconnectAttemps;

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
        checkInstance();
        currentActivity = activity;
        checkSettings();
    }

    public void unregisterCurrentActivity() {
        checkInstance();
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

    private void checkSettings() {
        if (currentActivity instanceof SettingsActivity) {
            return;
        }

        if (!settingsHelper.hasBluetoothSettings() && !settingsHelper.hasWifiSettings()) {
            launchSettingsActivity();
            Log.i(TAG, "No settings defined, opening SettingsActivity");

        } else if (mpdManager != null && !mpdManager.isConnected()) {
            mpdManager.connect();
        }
    }

    private void launchSettingsActivity() {
        Intent intent = new Intent(currentActivity, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public AbstractMPDManager getMpdManager() {
        if (settingsHelper.getSettings().isBluetooth()) {
            mpdManager = new BluetoothMPDManager();
        } else {
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
        if (reconnectAttemps < MAX_RECONNECT_ATTEMPTS) {
            Log.i(TAG, "Connection Failed, reconnecting: " + ++reconnectAttemps);
            mpdManager.connect();
        } else {
            showConnectionFailedDialog(message);
        }
    }

    private void showConnectionFailedDialog(String message) {
        if (currentActivity == null || currentActivity instanceof SettingsActivity) {
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
        builder.setMessage(getString(R.string.connectionFailedDialogMessage) + message)
                .setTitle(getString(R.string.connectionFailedDialogTitle));
        builder.setPositiveButton(getString(R.string.connectionFailedSettingsOption),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        launchSettingsActivity();
                    }
                });
        builder.setNegativeButton(getString(R.string.connectionFailedQuitOption),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentActivity.finish();
                    }
                });
        builder.setNeutralButton(getString(R.string.connectionFailedRetryOption),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reconnectAttemps = 0;
                        mpdManager.connect();
                    }
                });

        currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }

    @Override
    public void connectionSucceeded(String message) {
        Log.i(TAG, "Connection succeeded");
        reconnectAttemps = 0;
    }

    private static void checkInstance() {
        if (instance == null)
            throw new IllegalStateException("Application not created yet!");
    }
}

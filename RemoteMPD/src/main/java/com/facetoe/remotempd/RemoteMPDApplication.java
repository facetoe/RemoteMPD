package com.facetoe.remotempd;

import android.app.Activity;
import android.app.Application;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.facetoe.remotempd.helpers.MPDAsyncHelper;
import com.facetoe.remotempd.helpers.RMPDAlertDialogFragmentFactory;
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
    private DialogFragment dialog;
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
        currentActivity = activity;
        checkState();
    }

    private void checkState() {
        if (currentActivity instanceof SettingsActivity) {
            return;
        }

        if (settingsHelper.getSettings().isBluetooth() && !settingsHelper.hasBluetoothSettings()) {
            dialog = RMPDAlertDialogFragmentFactory.getNoBluetoothSettingsDialog();
            dialog.show(currentActivity.getFragmentManager(), "dialog");
            return;

        } else if (!settingsHelper.hasBluetoothSettings() && !settingsHelper.hasWifiSettings()) {
            dialog = RMPDAlertDialogFragmentFactory.getNoSettingsDialog();
            dialog.show(currentActivity.getFragmentManager(), "dialog");
            return;

        } else if (!settingsHelper.getSettings().isBluetooth() && !settingsHelper.hasWifiSettings()) {
            dialog = RMPDAlertDialogFragmentFactory.getNoWifiSettingsDialog();
            dialog.show(currentActivity.getFragmentManager(), "dialog");
            return;
        }

        checkConnection();
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
        if (key.equals(getString(R.string.remoteMpdConnectionTypeKey))) { //TODO change this to a constant
            fireMPDManagerChanged();
        }
    }

    @Override
    public void connectionFailed(String message) {
        hideConnectionProgressDialog();
        maybeShowConnectionFailedDialog(message);
    }

    @Override
    public void connectionSucceeded(String message) {
        hideConnectionProgressDialog();
        Log.i(TAG, "Connection succeeded: " + message);
    }

    public void showConnectingProgressDialog() {
        if (dialogIsVisible() || currentActivity instanceof SettingsActivity) {
            return;
        }
        dialog = RMPDAlertDialogFragmentFactory.getConnectionProgressDialog();
        dialog.show(currentActivity.getFragmentManager(), "dialog");
    }

    public void hideConnectionProgressDialog() {
        if (dialog != null && (dialog.getDialog() instanceof ProgressDialog)) {
            currentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.getDialog().dismiss();
                }
            });
        }
    }

    private void maybeShowConnectionFailedDialog(String message) {
        if (currentActivity == null
                || currentActivity instanceof SettingsActivity
                || mpdManager.isConnected()
                || dialogIsVisible()) {
            return;
        }

        dialog = RMPDAlertDialogFragmentFactory.getConnectionFailedDialog(message);
        dialog.show(currentActivity.getFragmentManager(), "dialog");
    }


    private boolean dialogIsVisible() {
        return dialog != null && dialog.getDialog() != null && dialog.isVisible();
    }

    private static void checkInstance() {
        if (instance == null)
            throw new IllegalStateException("Application not created yet!");
    }
}

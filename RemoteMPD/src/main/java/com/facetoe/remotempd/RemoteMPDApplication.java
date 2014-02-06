package com.facetoe.remotempd;

import android.app.*;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.facetoe.remotempd.helpers.RMPDAlertDialogFragmentFactory;
import com.facetoe.remotempd.helpers.SettingsHelper;
import com.facetoe.remotempd.listeners.MPDManagerChangeListener;

import java.util.ArrayList;

/**
 * Created by facetoe on 31/12/13.
 */

public class RemoteMPDApplication extends Application implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static RemoteMPDApplication instance;
    public final static String APP_PREFIX = "RMPD-";
    private static final String TAG = APP_PREFIX + "RemoteMPDApplication";

    private SettingsHelper settingsHelper;
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

    public void unregisterCurrentActivity() {
        Log.d(TAG, "Unregistering: " + currentActivity);
        currentActivity = null;
    }

    private void checkState() {
        if (currentActivity instanceof SettingsActivity) {
            return;
        }

        // Show Bluetooth specific dialog.
        if (settingsHelper.getSettings().isBluetooth() && !settingsHelper.hasBluetoothSettings()) {
            DialogFragment dialog = RMPDAlertDialogFragmentFactory.getNoBluetoothSettingsDialog();
            showDialog(dialog);
            return;

            // No settings at all, show no settings dialog
        } else if (!settingsHelper.hasBluetoothSettings() && !settingsHelper.hasWifiSettings()) {
            DialogFragment dialog = RMPDAlertDialogFragmentFactory.getNoSettingsDialog();
            showDialog(dialog);
            return;

            // Show wifi specific dialog.
        } else if (!settingsHelper.getSettings().isBluetooth() && !settingsHelper.hasWifiSettings()) {
            DialogFragment dialog = RMPDAlertDialogFragmentFactory.getNoWifiSettingsDialog();
            showDialog(dialog);
            return;
        }
        checkConnection();
    }

    private void checkConnection() {
        if (mpdManager != null && !mpdManager.isConnected()) {
            mpdManager.connect();
        }
    }

    private void showDialog(DialogFragment dialog) {
        dismissDialog();
        FragmentTransaction ft = currentActivity.getFragmentManager().beginTransaction();
        Fragment prev = currentActivity.getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dialog.show(currentActivity.getFragmentManager(), "dialog");
    }

    public void dismissDialog() {
        FragmentTransaction ft = currentActivity.getFragmentManager().beginTransaction();
        Fragment fragment = currentActivity.getFragmentManager().findFragmentByTag("dialog");
        if (fragment != null) {
            DialogFragment dialog = (DialogFragment) fragment;
            if (dialog.getDialog() != null) {
                dialog.getDialog().dismiss();
            }
            ft.remove(fragment);
        }
        ft.addToBackStack(null);
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

    public void notifyConnectionFailed(String message) {
        if(currentActivity != null) {
            dismissDialog();
            maybeShowConnectionFailedDialog(message);
        }
    }

    public void notifyConnectionSucceeded(String message) {
        if(currentActivity != null) {
            dismissDialog();
        }
    }

    private void maybeShowConnectionFailedDialog(String message) {
        if (currentActivity == null
                || currentActivity instanceof SettingsActivity
                || mpdManager.isConnected()) {
            return;
        }
        DialogFragment dialog = RMPDAlertDialogFragmentFactory.getConnectionFailedDialog(message);
        showDialog(dialog);
    }

    public void showConnectingProgressDialog() {
        if (currentActivity instanceof SettingsActivity) {
            return;
        }
        DialogFragment dialog = RMPDAlertDialogFragmentFactory.getConnectionProgressDialog();
        showDialog(dialog);
    }

    public void addMpdManagerChangeListener(MPDManagerChangeListener listener) {
        mpdManagerChangeListeners.add(listener);
    }

    public void removeMpdManagerChangeListener(MPDManagerChangeListener listener) {
        mpdManagerChangeListeners.remove(listener);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i(TAG, "Shared preferences changed: " + key);
        if (key.equals(getString(R.string.remoteMpdConnectionTypeKey))) { //TODO change this to a constant
            fireMPDManagerChanged();
        }
    }

    private void fireMPDManagerChanged() {
        for (MPDManagerChangeListener listener : mpdManagerChangeListeners) {
            listener.mpdManagerChanged();
        }
    }

    private static void checkInstance() {
        if (instance == null)
            throw new IllegalStateException("Application not created yet!");
    }
}

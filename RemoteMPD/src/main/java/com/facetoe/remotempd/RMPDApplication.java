package com.facetoe.remotempd;

import android.app.*;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import com.facetoe.remotempd.helpers.MPDAsyncHelper;
import com.facetoe.remotempd.helpers.RMPDAlertDialogFragmentFactory;
import com.facetoe.remotempd.helpers.SettingsHelper;
import com.facetoe.remotempd.helpers.WifiConnectionAsyncTask;
import com.facetoe.remotempd.listeners.MPDManagerChangeListener;
import org.a0z.mpd.ConnectionListener;
import org.a0z.mpd.MPD;
import org.a0z.mpd.MPDStatus;
import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.event.TrackPositionListener;

import java.util.ArrayList;

/**
 * Created by facetoe on 31/12/13.
 */

public class RMPDApplication extends Application implements
        SharedPreferences.OnSharedPreferenceChangeListener, ConnectionListener {

    private static RMPDApplication instance;
    public final static String APP_PREFIX = "RMPD-";
    private static final String TAG = APP_PREFIX + "RMPDApplication";
    public static final int REQUEST_ENABLE_BT = 2;

    private Activity currentActivity;
    private MPDAsyncHelper asyncHelper;

    public enum Event {
        CONNECT,
        CONNECTING,
        CONNECTION_FAILED,
        CONNECTION_SUCCEEDED,
        REFUSED_BT_ENABLE
    }

    private enum ConnectionState {
        CONNECTING,
        CONNECTED,
        DISCONNECTED
    }
    ConnectionState connectionState = ConnectionState.DISCONNECTED;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        asyncHelper = new MPDAsyncHelper();
        asyncHelper.addConnectionListener(this);
        MPD.setApplicationContext(getApplicationContext());
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    public static RMPDApplication getInstance() {
        checkInstance();
        return instance;
    }

    private static void checkInstance() {
        if (instance == null)
            throw new IllegalStateException("Application not created yet!");
    }

    public void registerCurrentActivity(Activity activity) {
        Log.d(TAG, "Register: " + activity);
        currentActivity = activity;
        checkState();
    }

    public void unregisterCurrentActivity() {
        Log.d(TAG, "Unregister: " + currentActivity);
        currentActivity = null;
    }

    public void notifyEvent(Event event) {
        switch (event) {
            case CONNECT:
                connect();
                break;
            case CONNECTING:
                showConnectingProgressDialog();
                break;
            case CONNECTION_SUCCEEDED:
                dismissDialog();
                break;
            case CONNECTION_FAILED:
                maybeShowConnectionFailedDialog("Failed to contact MPD server");
                break;
            case REFUSED_BT_ENABLE:
                showRefusedBluetoothEnableDialog();
                break;
            default:
                Log.w(TAG, "Unknown event: " + event);
                break;
        }
    }

    public void notifyEvent(Event event, String message) {
        switch (event) {
            case CONNECTION_FAILED:
                maybeShowConnectionFailedDialog(message);
                break;
            default:
                Log.w(TAG, "Unknown event: " + event);
                break;
        }
    }

    // TODO refactory this out to the SettingHelper
    public int getPref(String key, int defaultValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getInt(key, defaultValue);
    }

    public void setPref(String key, int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putInt(key, value).commit();
    }

    private void connect() {
        if(connectionState == ConnectionState.CONNECTING) {
            Log.w(TAG, "Connect called when connection already in progress.");
            return;
        }
        Log.i(TAG, "Connecting...");
        if(!asyncHelper.isMonitorAlive()) {
            asyncHelper.startMonitor();
        }
        setConnectionState(ConnectionState.CONNECTING);
        asyncHelper.connect();
        showConnectingProgressDialog();
    }

    public void disconnect() {
        asyncHelper.stopMonitor();
        asyncHelper.disconnect();
    }

    @Override
    public void connectionFailed(String message) {
        Log.i(TAG, "Connection failed: " + message);
        setConnectionState(ConnectionState.DISCONNECTED);
        maybeShowConnectionFailedDialog(message);
    }

    @Override
    public void connectionSucceeded(String message) {
        Log.i(TAG, "Connection succeeded: " + message);
        setConnectionState(ConnectionState.CONNECTED);
        dismissDialog();
    }

    public MPD getMpd() {
        return asyncHelper.oMPD;
    }

    public MPDAsyncHelper getAsyncHelper() {
        return asyncHelper;
    }

    private void checkState() {
        if (currentActivity instanceof SettingsActivity) {
            return;
        }

        // Show Bluetooth specific dialog.
        if (SettingsHelper.isBluetooth() && !SettingsHelper.hasBluetoothSettings()) {
            DialogFragment dialog = RMPDAlertDialogFragmentFactory.getNoBluetoothSettingsDialog();
            showDialog(dialog);
            return;

            // No settings at all, show no settings dialog.
        } else if (!SettingsHelper.hasBluetoothSettings() && !SettingsHelper.hasWifiSettings()) {
            DialogFragment dialog = RMPDAlertDialogFragmentFactory.getNoSettingsDialog();
            showDialog(dialog);
            return;

            // Show Wifi specific dialog.
        } else if (SettingsHelper.isWifi() && !SettingsHelper.hasWifiSettings()) {
            DialogFragment dialog = RMPDAlertDialogFragmentFactory.getNoWifiSettingsDialog();
            showDialog(dialog);
            return;
        }

        // Check that we can connect to MPD via Wifi or Bluetooth.
        // If not, enable the connection and connect,
        // if so, check that we are connected.
        if (SettingsHelper.isWifi() && !wifiIsEnabled()) {
            enableAndConnectWifi();
        } else if (SettingsHelper.isBluetooth() && !bluetoothIsEnabled()) {
            enableAndConnectBluetooth();
        } else {
            checkConnection();
        }
    }

    private boolean wifiIsEnabled() {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        return wifi.isWifiEnabled();
    }

    private boolean bluetoothIsEnabled() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter.isEnabled();
    }

    private void enableAndConnectWifi() {
        WifiConnectionAsyncTask task = new WifiConnectionAsyncTask(currentActivity);
        task.execute((Void) null);
    }

    private void enableAndConnectBluetooth() {
        Intent btEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        currentActivity.startActivityForResult(btEnableIntent, REQUEST_ENABLE_BT);
    }

    private void checkConnection() {
        if (!asyncHelper.oMPD.isConnected() && connectionState != ConnectionState.CONNECTING) {
            connect();
        }
    }

    private void showRefusedBluetoothEnableDialog() {
        DialogFragment dialog = RMPDAlertDialogFragmentFactory.getRefuseBluetoothEnableDialog();
        showDialog(dialog);
    }

    private void maybeShowConnectionFailedDialog(String message) {
        if (currentActivity == null
                || currentActivity instanceof SettingsActivity
                || asyncHelper.oMPD.isConnected()) {
            return;
        }
        DialogFragment dialog = RMPDAlertDialogFragmentFactory.getConnectionFailedDialog(message);
        showDialog(dialog);
    }

    private void showConnectingProgressDialog() {
        if (currentActivity instanceof SettingsActivity) {
            return;
        }
        DialogFragment dialog = RMPDAlertDialogFragmentFactory.getConnectionProgressDialog();
        showDialog(dialog);
    }

    private void showDialog(DialogFragment dialog) {
        if (currentActivity == null) {
            Log.e(TAG, "Can't show dialog, currentActivity is null");
            return;
        }
        dismissDialog();
        dialog.show(currentActivity.getFragmentManager(), "dialog");
    }

    private void dismissDialog() {
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i(TAG, "Shared preferences changed: " + key);
        if (key.equals(getString(R.string.remoteMpdConnectionTypeKey))) { //TODO change this to a constant
            asyncHelper.stopMonitor();
            asyncHelper.disconnect();
        }
    }

    private void setConnectionState(ConnectionState newState) {
        Log.d(TAG, "Connection state changed from " + connectionState + " to " + newState);
        connectionState = newState;
    }
}

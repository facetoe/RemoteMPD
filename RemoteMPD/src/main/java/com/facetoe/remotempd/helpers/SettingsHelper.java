package com.facetoe.remotempd.helpers;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.facetoe.remotempd.RemoteMPDApplication;
import com.facetoe.remotempd.RemoteMPDSettings;

/**
 * RemoteMPD
 * Created by facetoe on 2/02/14.
 */
public class SettingsHelper {
    public static final String CONNECTION_TYPE_KEY = "mpdConnectionType";
    public static final String BT_CONNECTION_TYPE_VALUE = "bluetooth";
    public static final String BT_LAST_BTDEVICE = "lastBTDevice";
    public static final String WIFI_CONNECTION_TYPE_VALUE = "wifi";
    public static final String WIFI_PORT_DEFAULT = "6600";

    private static final String WIFI_PORT_KEY = "mpdPort";
    private static final String WIFI_PASS_KEY = "mpdPassword";
    private static final String WIFI_HOST_KEY = "mpdIp";

    RemoteMPDApplication appInstance;

    public SettingsHelper(RemoteMPDApplication appInstance) {
        assert appInstance != null;
        this.appInstance = appInstance;
    }

    public RemoteMPDSettings getSettings() {
        RemoteMPDSettings mpdSettings = new RemoteMPDSettings();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appInstance);
        mpdSettings.setBluetooth(isBluetooth(prefs));
        mpdSettings.setHost(getWifiIP(prefs));
        mpdSettings.setPassword(getWifiMPDPassword(prefs));
        mpdSettings.setPort(Integer.parseInt(getWifiPort(prefs)));
        mpdSettings.setLastDevice(getLastBTDeviceAddress(prefs));
        return mpdSettings;
    }

    public boolean hasWifiSettings() {
        return !getSettings().getHost().trim().isEmpty();
    }

    public boolean hasBluetoothSettings() {
        return !getSettings().getLastDevice().trim().isEmpty();
    }

    private boolean isBluetooth(SharedPreferences prefs) {
        String prefValue = prefs.getString(CONNECTION_TYPE_KEY, "none");
        return prefValue.equals(BT_CONNECTION_TYPE_VALUE);
    }

    private String getWifiPort(SharedPreferences prefs) {
        return prefs.getString(WIFI_PORT_KEY, WIFI_PORT_DEFAULT);
    }

    private String getWifiIP(SharedPreferences prefs) {
        return prefs.getString(WIFI_HOST_KEY, "");
    }

    private String getWifiMPDPassword(SharedPreferences prefs) {
        return prefs.getString(WIFI_PASS_KEY, "");
    }

    private String getLastBTDeviceAddress(SharedPreferences prefs) {
        return prefs.getString(BT_LAST_BTDEVICE, "");
    }
}

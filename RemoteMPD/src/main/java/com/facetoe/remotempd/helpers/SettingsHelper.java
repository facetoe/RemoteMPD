package com.facetoe.remotempd.helpers;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.facetoe.remotempd.RMPDApplication;

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

    public static boolean hasWifiSettings() {
        return !getSettings().getHost().trim().isEmpty();
    }

    public static boolean hasBluetoothSettings() {
        return !getSettings().getLastDevice().trim().isEmpty();
    }

    public static boolean isBluetooth() {
        return getSettings().isBluetooth();
    }

    public static boolean isWifi() {
        return !getSettings().isBluetooth();
    }

    public static String getHost() {
        return getSettings().getHost();
    }

    public static String getPassword() {
        return getSettings().getPassword();
    }

    public static int getPort() {
        return getSettings().getPort();
    }

    public static String getLastDevice() {
        return getSettings().getLastDevice();
    }

    private static RemoteMPDSettings getSettings() {
        RemoteMPDSettings mpdSettings = new RemoteMPDSettings();
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(RMPDApplication.getInstance());

        String btPrefValue = prefs.getString(CONNECTION_TYPE_KEY, "none");
        mpdSettings.isBluetooth = btPrefValue.equals(BT_CONNECTION_TYPE_VALUE);
        mpdSettings.host = prefs.getString(WIFI_HOST_KEY, "");
        mpdSettings.password = prefs.getString(WIFI_PASS_KEY, "");
        mpdSettings.port = Integer.parseInt(prefs.getString(WIFI_PORT_KEY, WIFI_PORT_DEFAULT));
        mpdSettings.lastDevice = prefs.getString(BT_LAST_BTDEVICE, "");
        return mpdSettings;
    }

    private static class RemoteMPDSettings {
        private String host = "";
        private int port;
        private String password = "";
        private boolean isBluetooth;
        private String lastDevice = "";

        public RemoteMPDSettings() {
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getPassword() {
            return password;
        }

        public boolean isBluetooth() {
            return isBluetooth;
        }

        public String getLastDevice() {
            return lastDevice;
        }

        @Override
        public String toString() {
            return "\nRemoteMPDSettings{" +
                    "\nhost='" + host + '\'' +
                    "\nport=" + port +
                    "\npassword='" + password + '\'' +
                    "\nisBluetooth=" + isBluetooth +
                    "\nlastDevice='" + lastDevice + '\'' +
                    '}';
        }
    }
}

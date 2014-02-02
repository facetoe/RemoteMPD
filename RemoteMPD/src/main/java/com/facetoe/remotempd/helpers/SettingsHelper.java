package com.facetoe.remotempd.helpers;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.facetoe.remotempd.R;
import com.facetoe.remotempd.RemoteMPDApplication;
import com.facetoe.remotempd.RemoteMPDSettings;

/**
 * RemoteMPD
 * Created by facetoe on 2/02/14.
 */
public class SettingsHelper {

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
        return mpdSettings;
    }

    private boolean isBluetooth(SharedPreferences prefs) {
        String connectionKey = getResString(R.string.remoteMpdConnectionTypeKey);
        String bluetoothValue = getResString(R.string.bluetoothConSettingsValue);
        String prefValue = prefs.getString(connectionKey, "none");
        return prefValue.equals(bluetoothValue);
    }

    private String getWifiPort(SharedPreferences prefs) {
        String portKey = getResString(R.string.wifiPortKey);
        String defaultPortVal = getResString(R.string.wifiPortDefault);
        return prefs.getString(portKey, defaultPortVal);
    }

    private String getWifiIP(SharedPreferences prefs) {
        String ipKey = getResString(R.string.wifIpKey);
        return prefs.getString(ipKey, "");
    }

    private String getWifiMPDPassword(SharedPreferences prefs) {
        String passwordKey = getResString(R.string.wifiPassKey);
        return prefs.getString(passwordKey, "");
    }

    private String getResString(int resID) {
        return appInstance.getString(resID);
    }
}

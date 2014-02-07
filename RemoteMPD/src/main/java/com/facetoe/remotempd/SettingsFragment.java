package com.facetoe.remotempd;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import com.facetoe.remotempd.helpers.SettingsHelper;

import java.util.Set;

/**
 * RemoteMPDNew
 * Created by facetoe on 1/02/14.
 */
public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = RemoteMPDApplication.APP_PREFIX + "SettingsFragment";
    public static final int REQUEST_SCAN_DEVICE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        initDeviceScanListener();
    }

    private void initDeviceScanListener() {
        Preference bluetoothScan = findPreference(getString(R.string.bluetoothScanKey));
        final Intent deviceScanIntent = new Intent(getActivity(), DeviceListActivity.class);
        if (bluetoothScan != null) {
            bluetoothScan.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivityForResult(deviceScanIntent, REQUEST_SCAN_DEVICE);
                    return false;
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SettingsFragment.REQUEST_SCAN_DEVICE && resultCode == Activity.RESULT_OK) {
            if (data.hasExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS)) {
                String bluetoothAddress = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                saveBTDevice(bluetoothAddress);
            }
        }
    }

    private void saveBTDevice(String address) {
        Log.d(TAG, "Saving device: " + address);
        Activity parentActivity = getActivity();
        if(parentActivity != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(parentActivity);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(SettingsHelper.BT_LAST_BTDEVICE, address);
            editor.commit();
        }
    }
}

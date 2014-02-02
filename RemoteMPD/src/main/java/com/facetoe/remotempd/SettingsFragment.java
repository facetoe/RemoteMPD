package com.facetoe.remotempd;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

/**
 * RemoteMPDNew
 * Created by facetoe on 1/02/14.
 */
public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = RemoteMPDApplication.APP_PREFIX + "SettingsFragment";
    public static final int REQUEST_SCAN_FOR_DEVICE = 0;

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
                    startActivityForResult(deviceScanIntent, REQUEST_SCAN_FOR_DEVICE);
                    return false;
                }
            });
        }
    }
}

package com.facetoe.remotempd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * RemoteMPDNew
 * Created by facetoe on 1/02/14.
 */
public class SettingsActivity extends PreferenceActivity {
    private static final String TAG = RemoteMPDApplication.APP_PREFIX + "SettingsActivity";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SettingsFragment.REQUEST_SCAN_FOR_DEVICE) {
            if(resultCode == Activity.RESULT_OK) {

            }
        }
    }
}
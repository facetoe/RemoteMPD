package com.facetoe.remotempd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

/**
 * RemoteMPDNew
 * Created by facetoe on 1/02/14.
 */
public class SettingsActivity extends PreferenceActivity {
    private static final String TAG = RemoteMPDApplication.APP_PREFIX + "SettingsActivity";
    private RemoteMPDApplication app = RemoteMPDApplication.getInstance();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app.registerCurrentActivity(this);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        app.unregisterCurrentActivity();

    }
}
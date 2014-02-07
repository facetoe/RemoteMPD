package com.facetoe.remotempd;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * RemoteMPDNew
 * Created by facetoe on 1/02/14.
 */
public class SettingsActivity extends PreferenceActivity {
    private static final String TAG = RMPDApplication.APP_PREFIX + "SettingsActivity";
    private RMPDApplication app = RMPDApplication.getInstance();

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
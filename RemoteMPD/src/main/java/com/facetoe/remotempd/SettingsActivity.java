package com.facetoe.remotempd;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * RemoteMPDNew
 * Created by facetoe on 1/02/14.
 */
public class SettingsActivity extends PreferenceActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
package com.facetoe.remotempd;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * RemoteMPDNew
 * Created by facetoe on 1/02/14.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}

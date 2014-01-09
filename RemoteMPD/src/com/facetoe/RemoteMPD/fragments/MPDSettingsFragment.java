package com.facetoe.RemoteMPD.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.facetoe.RemoteMPD.R;

/**
 * Created by facetoe on 7/01/14.
 */
public class MPDSettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}

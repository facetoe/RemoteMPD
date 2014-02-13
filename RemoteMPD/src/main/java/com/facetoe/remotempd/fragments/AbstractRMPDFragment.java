package com.facetoe.remotempd.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.facetoe.remotempd.AbstractMPDManager;
import com.facetoe.remotempd.RMPDApplication;
import com.facetoe.remotempd.listeners.MPDManagerChangeListener;

/**
 * RemoteMPD
 * Created by facetoe on 11/02/14.
 *
 * Base class for RMPDFragments. Handles MPDManager changes.
 */
public abstract class AbstractRMPDFragment extends Fragment implements MPDManagerChangeListener {
    private static final String TAG = RMPDApplication.APP_PREFIX + "AbstractRMPDFragment";
    protected RMPDApplication app = RMPDApplication.getInstance();
    protected AbstractMPDManager mpdManager = app.getMpdManager();

    @Override
    public void mpdManagerChanged() {
        Log.i(TAG, "MPDManagerChanged()");
        mpdManager.disconnect();
        mpdManager = app.getMpdManager();
    }
}
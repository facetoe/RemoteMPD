package com.facetoe.remotempd;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import com.facetoe.remotempd.helpers.SettingsHelper;

/**
 * Created by facetoe on 31/12/13.
 */

public class RemoteMPDApplication extends Application implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static RemoteMPDApplication instance;
    public final static String APP_PREFIX = "RMPD-";
    private SettingsHelper settingsHelper;
    private Activity currentActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        settingsHelper = new SettingsHelper(this);
    }

    public static RemoteMPDApplication getInstance() {
        checkInstance();
        return instance;
    }

    public void registerCurrentActivity(Activity activity) {
        checkInstance();
        currentActivity = activity;
    }

    public void unregisterCurrentActivity() {
        checkInstance();
        currentActivity = null;
    }

    public RemoteMPDSettings getSettings() {
        return settingsHelper.getSettings();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }



    private static void checkInstance() {
        if (instance == null)
            throw new IllegalStateException("Application not created yet!");
    }
}

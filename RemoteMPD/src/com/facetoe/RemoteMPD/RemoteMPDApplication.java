package com.facetoe.RemoteMPD;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import com.facetoe.RemoteMPD.helpers.MPDAsyncHelper;
import com.facetoe.RemoteMPD.tools.NetworkHelper;
import org.a0z.mpd.Music;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by facetoe on 31/12/13.
 */
public class RemoteMPDApplication extends Application implements MPDAsyncHelper.ConnectionListener {
    public final static String APP_TAG = "RemoteMPDApplication";
    private static final String PREFERENCES = "preferences";

    public static boolean isBluetooth = false;
    private Activity currentActivity;
    private Collection<Object> connectionLocks = new LinkedList<Object>();
    AlertDialog ad;

    private static RemoteMPDApplication instance;
    List<Music> songList;
    SharedPreferences sharedPreferences;
    public MPDAsyncHelper asyncHelper;
    private AbstractMPDManager mpdManager;

    class DialogClickListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_NEUTRAL:
                    // Show Settings
                    //currentActivity.startActivityForResult(new Intent(currentActivity, WifiConnectionSettings.class), SETTINGS);
                    break;
                case AlertDialog.BUTTON_NEGATIVE:
                    currentActivity.finish();
                    break;
                case AlertDialog.BUTTON_POSITIVE:
                    connectMPD();
                    break;

            }
        }
    }

    public static RemoteMPDApplication getInstance() {
        checkInstance();
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(APP_TAG, "Initializing application..");
        asyncHelper = new MPDAsyncHelper();
        asyncHelper.addConnectionListener(this);
        instance = this;
    }

    public void connectMPD() {
        Log.i(APP_TAG, "Connecting MPD");
        // check for network
        if (!NetworkHelper.isNetworkConnected(this.getApplicationContext())) {
            connectionFailed("No network.");
            return;
        }

        // show connecting to server dialog
        if (currentActivity != null) {
            ad = new ProgressDialog(currentActivity);
            ad.setTitle("Connecting"); //TODO add as resource
            ad.setMessage("Connecting to server"); //TODO add as resource
            ad.setCancelable(false);
            ad.setOnKeyListener(new DialogInterface.OnKeyListener() {
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    // Handle all keys!
                    return true;
                }
            });
            try {
                ad.show();
            } catch (WindowManager.BadTokenException e) {
                // Can't display it. Don't care.
            }
        }
        asyncHelper.connect();
    }

    public List<Music> getSongList() {
        checkInstance();
        return songList;
    }

    public void setSongList(List<Music> songList) {
        this.songList = songList;
    }

    public SharedPreferences getSharedPreferences() {
        checkInstance();
        if (sharedPreferences == null)
            sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        return sharedPreferences;
    }

    public AbstractMPDManager getMpdManager() {
        if (isBluetooth && mpdManager == null)
            mpdManager = new BluetoothMPDManager();
        else if (!isBluetooth && mpdManager == null)
            mpdManager = new WifiMPDManager();

        return mpdManager;
    }

    public void setMpdManager(AbstractMPDManager mpdManager) {
        this.mpdManager = mpdManager;
    }

    private static void checkInstance() {
        if (instance == null)
            throw new IllegalStateException("Application not created yet!");
    }

    @Override
    public void connectionFailed(String message) {
        if(isBluetooth) {

        } else {
            handleWifiConnectionFailed(message);
        }
    }

    private void handleWifiConnectionFailed(String message) {
        if (ad != null && !(ad instanceof ProgressDialog) && ad.isShowing()) {
            return;
        }

        // dismiss possible dialog
        dismissAlertDialog();

        asyncHelper.disconnect();

        if (currentActivity == null) {
            return;
        }

        Log.e(APP_TAG, "Connection failed: " + message);
        AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
        builder.setTitle("Connection failed");
        builder.setMessage(message);
        builder.setCancelable(false);

        DialogClickListener listener = new DialogClickListener();
        builder.setNegativeButton("Quit", listener);
        builder.setNeutralButton("Settings", listener);
        builder.setPositiveButton("Reconnect", listener);
        try {
            ad = builder.show();
        } catch (WindowManager.BadTokenException e) {
            // Can't display it. Don't care.
        }
    }

    @Override
    public void connectionSucceeded(String message) {
        Log.e(APP_TAG, "Connection succeeded! " + message);
        dismissAlertDialog();
    }

    private void dismissAlertDialog() {
        if (ad != null) {
            if (ad.isShowing()) {
                try {
                    ad.dismiss();
                } catch (IllegalArgumentException e) {
                    // We don't care, it has already been destroyed
                }
            }
        }
    }

    public void setCurrentActivity(Activity currentActivity) {
        this.currentActivity = currentActivity;
        connectionLocks.add(currentActivity);
    }

    public void unsetActivity() {
        connectionLocks.remove(currentActivity);
        this.currentActivity = null;
    }

}

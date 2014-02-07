package com.facetoe.remotempd.helpers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import com.facetoe.remotempd.RMPDApplication;

import java.util.Timer;
import java.util.TimerTask;

/**
 * RemoteMPD
 * Created by facetoe on 7/02/14.
 * <p/>
 * This class enables and connects to the Wifi network. It presents a non-cancelable dialog with
 * a progress spinner. If no connection is established within CONNECTION_TIMEOUT, the user is directed to
 * the Android Wifi settings in order to choose a network. if a connection is established within CONNECTION_TIMEOUT
 * the dialog is dismissed and control is returned to the calling activity.
 * <p/>
 * Regardless of whether or not the connection succeeded, connect() is called in onPostExecute.
 * This means a failed connection will result in an error on returning to the calling activity.
 */
public class WifiConnectionAsyncTask extends AsyncTask<Void, String, Boolean> {
    Activity activity;
    ProgressDialog dialog;
    private static final String TAG = RMPDApplication.APP_PREFIX + "WifiConnectionAsyncTask";
    private static final int CONNECTION_TIMEOUT = 10000;
    private boolean keepWaiting = true;
    private Timer timer;


    public WifiConnectionAsyncTask(Activity currentActivity) {
        this.activity = currentActivity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.i(TAG, "onPreExecute");
        dialog = new ProgressDialog(activity);
        dialog.setTitle("Connecting to Wifi");
        dialog.setMessage("Please wait, connecting to Wifi network");
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        Log.i(TAG, "onPostExecute");
        dialog.dismiss();
        timer.cancel();
        RMPDApplication.getInstance().notifyEvent(RMPDApplication.Event.CONNECT);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        enableWifi();
        initTimer();
        while (!isConnected() && keepWaiting) {
            Log.d(TAG, "Not connected...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.w(TAG, "Interrupted");
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    private void enableWifi() {
        WifiManager wifi = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled()) {
            wifi.setWifiEnabled(true);
        }
    }

    private void initTimer() {
        timer = new Timer("connectionTimer");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
                activity.startActivity(intent);
                keepWaiting = false;
            }
        }, CONNECTION_TIMEOUT);
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }

        return networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED;
    }
}
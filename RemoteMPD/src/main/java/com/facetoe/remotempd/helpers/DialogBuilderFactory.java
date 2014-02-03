package com.facetoe.remotempd.helpers;

/**
 * RemoteMPD
 * Created by facetoe on 3/02/14.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import com.facetoe.remotempd.R;
import com.facetoe.remotempd.RemoteMPDApplication;
import com.facetoe.remotempd.SettingsActivity;

/**
 * Creates various dialogs. The reason this class is a DialogBuilder factory as opposed to a
 * DialogFactory is calling Builder.create() while not on the UI thread throws an Exception.
 *
 */
public class DialogBuilderFactory {

    public static AlertDialog.Builder getConnectionFailedDialog(final Activity activity, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final RemoteMPDApplication app = RemoteMPDApplication.getInstance();

        builder.setMessage(activity.getString(R.string.connectionFailedDialogMessage) + message)
                .setTitle(activity.getString(R.string.connectionFailedDialogTitle));
        builder.setPositiveButton(activity.getString(R.string.dialogOpenSettingsOption),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        launchSettingsActivity(activity);
                    }
                });
        builder.setNegativeButton(activity.getString(R.string.connectionFailedQuitOption),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                    }
                });
        builder.setNeutralButton(activity.getString(R.string.connectionFailedRetryOption),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        app.getMpdManager().connect();
                    }
                });

        return builder;
    }

    public static AlertDialog.Builder getNoSettingsDialog(final Activity activity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.noSettingsDialogTitle));
        builder.setMessage(activity.getString(R.string.noSettingsDialogMessage));
        builder.setPositiveButton(activity.getString(R.string.dialogOpenSettingsOption),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        launchSettingsActivity(activity);
                    }
                });
        builder.setNegativeButton(activity.getString(R.string.noSettingsDialogQuitOption),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                    }
                });

        return builder;
    }

    public static AlertDialog.Builder getNoBluetoothSettingsDialog(final Activity activity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.noBluetoothDeviceDialogTitle));
        builder.setMessage(activity.getString(R.string.noBluetoothDeviceDialogMessage));
        builder.setPositiveButton(activity.getString(R.string.dialogOpenSettingsOption), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                launchSettingsActivity(activity);
            }
        });
        builder.setNegativeButton(activity.getString(R.string.dialogQuitOption), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.finish();
            }
        });

        return builder;
    }

    public static AlertDialog.Builder getNoWifiSettingsDialog(final Activity activity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.noWifiSettingsDialogTitle));
        builder.setMessage(activity.getString(R.string.noWifiSettingsDialogMessage));
        builder.setPositiveButton(activity.getString(R.string.dialogOpenSettingsOption),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        launchSettingsActivity(activity);
                    }
                });
        builder.setNegativeButton(activity.getString(R.string.dialogQuitOption),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                    }
                });
        return builder;
    }

    private static void launchSettingsActivity(Activity currentActivity) {
        Intent intent = new Intent(currentActivity, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        currentActivity.startActivity(intent);
    }
}

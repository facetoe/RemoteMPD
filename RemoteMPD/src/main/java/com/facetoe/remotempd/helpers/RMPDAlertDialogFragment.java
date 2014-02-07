package com.facetoe.remotempd.helpers;

import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import com.facetoe.remotempd.R;
import com.facetoe.remotempd.RemoteMPDApplication;
import com.facetoe.remotempd.SettingsActivity;

/**
 * RemoteMPD
 * Created by facetoe on 4/02/14.
 */
public class RMPDAlertDialogFragment extends DialogFragment {

    private static final String TITLE = "title";
    private static final String MESSAGE = "message";

    private static final String DIALOG_TYPE_KEY = "dialogType";
    private static final int CONNECTION_FAILED = 0;
    private static final int NO_SETTINGS = 1;
    private static final int NO_BLUETOOTH_SETTINGS = 2;
    private static final int NO_WIFI_SETTINGS = 3;
    private static final int CONNECTION_PROGRESS = 4;
    private static final int REFUSE_BT_ENABLE = 5;

    private static final String TAG = RemoteMPDApplication.APP_PREFIX + "RMPDAlertDialogFragment";

    /**
     * This shouldn't be called, use the static methods to create a dialog.
     */
    public RMPDAlertDialogFragment() {
    }

    public static RMPDAlertDialogFragment getConnectionFailedDialog(String message) {
        return createDialogFragment(CONNECTION_FAILED, message);
    }

    public static RMPDAlertDialogFragment getNoSettingsDialog() {
        return createDialogFragment(NO_SETTINGS);
    }

    public static RMPDAlertDialogFragment getNoBluetoothSettingsDialog() {
        return createDialogFragment(NO_BLUETOOTH_SETTINGS);
    }

    public static RMPDAlertDialogFragment getNoWifiSettingsDialog() {
        return createDialogFragment(NO_WIFI_SETTINGS);
    }

    public static RMPDAlertDialogFragment getConnectionProgressDialog() {
        return createDialogFragment(CONNECTION_PROGRESS);
    }

    public static RMPDAlertDialogFragment getRefuseBluetoothEnableDialog() {
        return createDialogFragment(REFUSE_BT_ENABLE);
    }

    private static RMPDAlertDialogFragment createDialogFragment(int dialogType) {
        RMPDAlertDialogFragment fragment = new RMPDAlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt(DIALOG_TYPE_KEY, dialogType);
        fragment.setArguments(args);
        return fragment;
    }

    private static RMPDAlertDialogFragment createDialogFragment(int dialogType, String message) {
        RMPDAlertDialogFragment fragment = new RMPDAlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt(DIALOG_TYPE_KEY, dialogType);
        args.putString(MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        int dialogType = getArguments().getInt(DIALOG_TYPE_KEY);
        switch (dialogType) {
            case CONNECTION_FAILED:
                return createConnectionFailedDialog(getArguments().getString(MESSAGE));
            case NO_SETTINGS:
                return createNoSettingsDialog();
            case NO_BLUETOOTH_SETTINGS:
                return createNoBluetoothSettingsDialog();
            case NO_WIFI_SETTINGS:
                return createNoWifiSettingsDialog();
            case CONNECTION_PROGRESS:
                return createConnectionProgressDialog();
            case REFUSE_BT_ENABLE:
                return createRefusedBluetoothEnableDialog();
            default:
                Log.e(TAG, "Unknown dialog type: " + dialogType);
                return null;
        }
    }

    private Dialog createConnectionProgressDialog() {
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setTitle(getString(R.string.connectionProgressDialogTitle));
        dialog.setMessage(getString(R.string.connectionProgressDialogMessage));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        return dialog;
    }

    // TODO figure out how to open the Wifi settings directly from here
    private Dialog createNoWifiSettingsDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.noWifiSettingsDialogTitle));
        builder.setMessage(getString(R.string.noWifiSettingsDialogMessage));
        builder.setPositiveButton(getString(R.string.dialogOpenSettingsOption),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        launchSettingsActivity(getActivity());
                    }
                });
        builder.setNegativeButton(getString(R.string.dialogQuitOption),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                });
        return builder.create();
    }

    // TODO figure out how to open the Bluetooth settings directly from here
    private Dialog createNoBluetoothSettingsDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.noBluetoothDeviceDialogTitle));
        builder.setMessage(getString(R.string.noBluetoothDeviceDialogMessage));
        builder.setPositiveButton(getString(R.string.dialogOpenSettingsOption), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                launchSettingsActivity(getActivity());
            }
        });
        builder.setNegativeButton(getString(R.string.dialogQuitOption), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }
        });

        return builder.create();
    }

    private Dialog createRefusedBluetoothEnableDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Bluetooth Not Enabled");
        builder.setMessage("Bluetooth must be enabled to connect to the Bluetooth server");
        builder.setPositiveButton("Use Wifi", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                prefs.edit().putString(getString(R.string.remoteMpdConnectionTypeKey), "wifi").commit();
                RemoteMPDApplication.getInstance().notifyEvent(RemoteMPDApplication.Event.CONNECT);
            }
        });
        builder.setNegativeButton(getString(R.string.dialogQuitOption), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }
        });
        return builder.create();
    }

    private Dialog createConnectionFailedDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final RemoteMPDApplication app = RemoteMPDApplication.getInstance();
        builder.setMessage(getString(R.string.connectionFailedDialogMessage) + message)
                .setTitle(getString(R.string.connectionFailedDialogTitle));
        builder.setPositiveButton(getString(R.string.dialogOpenSettingsOption),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        launchSettingsActivity(getActivity());
                    }
                });
        builder.setNegativeButton(getString(R.string.connectionFailedQuitOption),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                });
        builder.setNeutralButton(getString(R.string.connectionFailedRetryOption),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        app.getMpdManager().connect();
                    }
                });
        return builder.create();
    }


    private Dialog createNoSettingsDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.noSettingsDialogTitle));
        builder.setMessage(getString(R.string.noSettingsDialogMessage));
        builder.setPositiveButton(getString(R.string.dialogOpenSettingsOption),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        launchSettingsActivity(getActivity());
                    }
                });
        builder.setNegativeButton(getString(R.string.noSettingsDialogQuitOption),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                });

        return builder.create();
    }

    private void launchSettingsActivity(Activity currentActivity) {
        Intent intent = new Intent(currentActivity, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        currentActivity.startActivity(intent);
    }
}


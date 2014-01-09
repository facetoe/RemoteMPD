package com.facetoe.RemoteMPD;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import com.facetoe.RemoteMPD.adapters.SongListAdapter;
import org.a0z.mpd.Music;

import java.util.List;
import java.util.prefs.Preferences;


public class RemoteMPDActivity extends ActionBarActivity {

    private static final String TAG = RemoteMPDApplication.APP_TAG;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CONNECT_DEVICE = 2;

    public static final String MESSAGE = "message";
    public static final String ERROR = "error";

    private ListView songListView;
    private SongListAdapter songListAdapter;
    private List<Music> songList;
    private static final RemoteMPDApplication myApp = RemoteMPDApplication.getInstance();
    SharedPreferences prefs;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

//        songListView = (ListView) findViewById(R.id.listSongs);
//        if (myApp.getSongList() == null) {
//            songList = new ArrayList<Music>();
//        } else {
//            songList = myApp.getSongList();
//        }
//        songListAdapter = new SongListAdapter(this, songList);
//        songListView.setAdapter(songListAdapter);
//        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Music item = songListAdapter.getItem(position);
//                mpdManager.playID(item.getSongId());
//            }
//        });


//        skVolume = (SeekBar) findViewById(R.id.skVolume);
//        skVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                mpdManager.setVolume(progress);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//            }
//        });

        // Register for broadcasts on BluetoothAdapter state change
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        prefs = myApp.getSharedPreferences();
        this.registerReceiver(mReceiver, filter);
        RemoteMPDApplication.getInstance().setCurrentActivity(this);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initializeBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            //TODO error message;
            // Bluetooth is not supported.
            finish();
        }

        /* If Bluetooth is turned off request that it be enabled. */
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void initializeWifi() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.i(TAG, "WiFI is connected");
        } else {
            Log.e(TAG, "No Wifi"); //TODO handle no wifi
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        RemoteMPDApplication.getInstance().unsetActivity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent myIntent = new Intent(this, SettingsActivity.class);
                startActivity(myIntent);
                return true;
        }
        return false;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

                    // Get the BLuetoothDevice object
                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

                    // Save device.
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(BluetoothController.LAST_DEVICE_KEY, device.getAddress());
                    editor.commit();
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_CANCELED) {
                    // User did not enable Bluetooth or an error occured
                    Toast.makeText(this, "Goodbye", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    //TODO Hook this in with the notification bar.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.i(TAG, "Bluetooth off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.i(TAG, "Bluetooth turning off");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.i(TAG, "Bluetooth on");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.i(TAG, "Bluetooth turning on");
                        break;
                }
            }
        }
    };
}

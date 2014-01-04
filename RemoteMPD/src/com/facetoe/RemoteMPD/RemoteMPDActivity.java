package com.facetoe.RemoteMPD;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.facetoe.RemoteMPD.adapters.SongListAdapter;
import com.google.gson.Gson;
import org.a0z.mpd.MPDCommand;
import org.a0z.mpd.MPDStatus;
import org.a0z.mpd.Music;
import org.a0z.mpd.event.StatusChangeListener;

import java.util.ArrayList;
import java.util.List;


public class RemoteMPDActivity extends Activity implements View.OnClickListener, StatusChangeListener {

    private static final String TAG = "RemoteMPDActivity";

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CONNECT_DEVICE = 2;

    public static final String MESSAGE = "message";
    public static final String ERROR = "error";
    BluetoothAdapter bluetoothAdapter;
    private boolean isBluetooth = true; //TODO remove this and have it in preferences
    private static final RemoteMPDApplication myApp = RemoteMPDApplication.getInstance();
    MPDPlayerController mpdManager;
    SharedPreferences prefs;
    Gson gson = new Gson();

    private ImageButton btnNext;
    private ImageButton btnPrev;
    private ImageButton btnPlay;
    private TextView txtCurrentSong;
    private TextView txtCurrentAlbum;
    private TextView txtCurrentArtist;
    private TextView txtElapsedTime;
    private TextView txtTotalTime;

    private ListView songListView;
    private SongListAdapter songListAdapter;
    private List<Music> songList;

    private SeekBar skVolume;
    private SeekBar skTrackPosition;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        txtCurrentAlbum = (TextView) findViewById(R.id.txtCurrentAlbum);
        txtCurrentSong = (TextView) findViewById(R.id.txtCurrentSong);

        btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(this);
        btnPrev = (ImageButton) findViewById(R.id.btnBack);
        btnPrev.setOnClickListener(this);
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(this);

        songListView = (ListView) findViewById(R.id.listSongs);
        if (myApp.getSongList() == null) {
            songList = new ArrayList<Music>();
        } else {
            songList = myApp.getSongList();
        }
        songListAdapter = new SongListAdapter(this, songList);
        songListView.setAdapter(songListAdapter);
        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Music item = songListAdapter.getItem(position);
                MPDCommand command = new MPDCommand(MPDCommand.MPD_CMD_PLAY_ID, Integer.toString(item.getSongId()));
                Log.i(TAG, "Sending: " + command.toString());
                // TODO Add the command to play id to the playermanger
                //commandService.sendCommand(command);
            }
        });


        skVolume = (SeekBar) findViewById(R.id.skVolume);
        skVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mpdManager.setVolume(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Register for broadcasts on BluetoothAdapter state change
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        prefs = myApp.getSharedPreferences();
        this.registerReceiver(mReceiver, filter);
    }

    @Override
    public void volumeChanged(MPDStatus mpdStatus, int oldVolume) {

    }

    @Override
    public void playlistChanged(MPDStatus mpdStatus, int oldPlaylistVersion) {

    }

    @Override
    public void trackChanged(MPDStatus mpdStatus, int oldTrack) {
        List<Music> music = myApp.getSongList();
        if (songList != null) {
            int pos = mpdStatus.getSongPos();
            Music song = music.get(pos);
            Log.e(TAG, "We got im: " + song);
        } else {
            Log.e(TAG, "Song list was null");
        }
    }

    @Override
    public void stateChanged(MPDStatus mpdStatus, String oldState) {

    }

    @Override
    public void repeatChanged(boolean repeating) {

    }

    @Override
    public void randomChanged(boolean random) {

    }

    @Override
    public void connectionStateChanged(boolean connected, boolean connectionLost) {

    }

    @Override
    public void libraryStateChanged(boolean updating) {

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
        if (isBluetooth) {
            initializeBluetooth();
            mpdManager = new BluetoothMPDManager();
            mpdManager.start();
        } else {
            initializeWifi();
            mpdManager = new WifiMPDManager();
            mpdManager.start();
        }

    }

    private void initializeBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        myApp.getBluetoothMonitor().addStatusChangeListener(this);
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
            myApp.asyncHelper.addStatusChangeListener(this);
        } else {
            Log.e(TAG, "No Wifi"); //TODO handle no wifi
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.find_devices, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.findDevices:
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
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
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);


                    //TODO make this connect through the service
                    //commandService.setDevice(device);
                    // Attempt to connect to the device
                    //commandService.connect();

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

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BluetoothController.MESSAGE_RECEIVED:
                    MPDResponse response = (MPDResponse) msg.getData().getSerializable(MESSAGE);
                    handleMessageReceived(response);
                    break;
            }
        }
    };

    private void handleMessageReceived(MPDResponse response) {
//        String jsonString = response.getObjectJSON();
//        switch (response.getResponseType()) {
//            case MPDResponse.PLAYER_UPDATE_CURRENTSONG:
//                Music newSong = gson.fromJson(jsonString, Music.class);
//                txtCurrentSong.setText(newSong.getTitle());
//                break;
//
//            case MPDResponse.PLAYER_UPDATE_TRACK_POSITION:
//                int time = gson.fromJson(response.getObjectJSON(), int.class);
//                //skTrackPosition.setProgress(time);
//                txtCurrentAlbum.setText(Integer.toString(time));
//                break;
//            case MPDResponse.PLAYER_UPDATE_PLAYLIST:
//                try {
//                    Type listType = new TypeToken<List<Music>>() {
//                    }.getType();
//                    List<Music> songList = gson.fromJson(response.getObjectJSON(), listType);
//                    songListAdapter.clear();
//                    songListAdapter.addAll(songList);
//                    songListAdapter.notifyDataSetChanged();
//                    myApp.setSongList(songList);
//                    Log.i(TAG, "Added " + songList.size() + " songs");
//                } catch (Exception e) {
//                    Log.e(TAG, "FUCK: ", e);
//                }
//                break;
//            default:
//                Log.i(TAG, "Unknown message type: " + response.getResponseType());
//                break;
//
//        }
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnNext:
                mpdManager.next();
                break;
            case R.id.btnBack:
                mpdManager.prev();
                break;
            case R.id.btnPlay:
                mpdManager.play();
                break;
            default:
                return;
        }
    }
}

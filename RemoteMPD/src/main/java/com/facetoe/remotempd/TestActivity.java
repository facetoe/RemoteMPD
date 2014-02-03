package com.facetoe.remotempd;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.*;
import android.widget.Button;

public class TestActivity extends ActionBarActivity {
    private static final String TAG = RemoteMPDApplication.APP_PREFIX + "TestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements View.OnClickListener,
            MPDManagerChangeListener {
        private Button btnPlay;
        private Button btnStop;
        private Button btnDisconnect;
        private Button btnConnect;
        private RemoteMPDApplication app = RemoteMPDApplication.getInstance();
        private AbstractMPDManager mpdManager;
        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_test, container, false);
            btnPlay = (Button) rootView.findViewById(R.id.btnPlay);
            btnStop = (Button) rootView.findViewById(R.id.btnStop);
            btnDisconnect = (Button) rootView.findViewById(R.id.btnDisconnect);
            btnConnect = (Button) rootView.findViewById(R.id.btnConnect);

            btnPlay.setOnClickListener(this);
            btnStop.setOnClickListener(this);
            btnDisconnect.setOnClickListener(this);
            btnConnect.setOnClickListener(this);

            mpdManager = app.getMpdManager();
            app.addMpdManagerChangeListener(this);

            return rootView;
        }

        @Override
        public void onStart() {
            super.onStart();
            app.registerCurrentActivity(getActivity());
        }

        @Override
        public void onStop() {
            super.onStop();
            app.unregisterCurrentActivity();
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnPlay:
                    mpdManager.play();
                    break;
                case R.id.btnStop:
                    mpdManager.stop();
                    break;
                case R.id.btnDisconnect:
                    mpdManager.disconnect();
                    break;
                case R.id.btnConnect:
                    mpdManager.connect();
                    break;
                default:
                    Log.i(TAG, "Unknown: " + view.getId());
            }
        }

        @Override
        public void mpdManagerChanged() {
            Log.i(TAG, "MPDManagerChanged()");
            mpdManager.disconnect();
            mpdManager = app.getMpdManager();
            mpdManager.connect();
        }
    }
}

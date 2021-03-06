package com.facetoe.remotempd;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import com.facetoe.remotempd.fragments.BrowserFragment;
import com.facetoe.remotempd.fragments.PlaylistFragment;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class TestActivity extends FragmentActivity {
    private static final String TAG = RMPDApplication.APP_PREFIX + "TestActivity";
    private final RMPDApplication app = RMPDApplication.getInstance();

    ViewPager viewPager;
    ListPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        setProgressBarIndeterminateVisibility(true);
        setContentView(R.layout.main_activity);

        SlidingUpPanelLayout slidingUpLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        RelativeLayout dragAnchor = (RelativeLayout)findViewById(R.id.dragAnchor);
        slidingUpLayout.setDragView(dragAnchor);
        slidingUpLayout.setPanelHeight(100);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        pagerAdapter = new ListPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                pagerAdapter.onFragmentVisible(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        app.registerCurrentActivity(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        app.unregisterCurrentActivity();
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            if (pagerAdapter.browserFragmentCanGoBack()) {
                pagerAdapter.browserFragmentBack();
            } else {
                super.onBackPressed();
            }
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "On activity result called");
        if (requestCode == RMPDApplication.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "Bluetooth enabled");
                app.notifyEvent(RMPDApplication.Event.CONNECT);
            } else {
                Log.i(TAG, "Bluetooth not enabled");
                app.notifyEvent(RMPDApplication.Event.REFUSED_BT_ENABLE);
            }
        }
    }

    // Call handleSearchEvents when we want the currently visible fragment
    // to handle search queries.
    public interface OnFragmentVisible {
        // Set up any other housekeeping for when the fragment becomes visible.
        void onVisible();
    }

    private class ListPagerAdapter extends FragmentPagerAdapter {
        private static final int NUM_FRAGMENTS = 2;
        BrowserFragment browserFragment;
        PlaylistFragment playlistFragment;

        public ListPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void onFragmentVisible(int fragmentIndex) {
            if(browserFragment == null || playlistFragment == null) {
                Log.e(TAG, "Error, fragments were null in onFragmentVisible");
                return;
            }
            if (fragmentIndex == 0) {
                browserFragment.onVisible();
            } else {
                playlistFragment.onVisible();
            }
        }

        public boolean browserFragmentCanGoBack() {
            return browserFragment.canGoBack();
        }

        public void browserFragmentBack() {
            browserFragment.goBack();
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                if (browserFragment == null) {
                    browserFragment = new BrowserFragment();
                }
                return browserFragment;
            } else {
                if (playlistFragment == null) {
                    playlistFragment = new PlaylistFragment();
                }
                return playlistFragment;
            }
        }

        // On orientation change the pager adapter saves the fragments and
        // then recreates them, however it does not call getItem again so playlistFragment
        // and browserFragment would both be null. By overriding this method
        // we can retain our references to them on orientation change.
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Object fragment = super.instantiateItem(container, position);
            if(fragment instanceof PlaylistFragment) {
                playlistFragment = (PlaylistFragment)fragment;
            } else if(fragment instanceof BrowserFragment) {
                browserFragment = (BrowserFragment)fragment;
            } else {
                Log.w(TAG, "Unknown object type in instantiateItem: " + fragment.getClass().getSimpleName());
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return NUM_FRAGMENTS;
        }
    }
}

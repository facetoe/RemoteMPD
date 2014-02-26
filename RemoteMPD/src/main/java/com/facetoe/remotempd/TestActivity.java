package com.facetoe.remotempd;


import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import com.facetoe.remotempd.fragments.AbstractListFragment;
import com.facetoe.remotempd.fragments.PlaylistFragment;
import com.facetoe.remotempd.fragments.TestFragment;
import org.a0z.mpd.Item;

public class TestActivity extends FragmentActivity {
    private static final String TAG = RMPDApplication.APP_PREFIX + "TestActivity";
    private final RMPDApplication app = RMPDApplication.getInstance();
    SearchView searchView;
    MenuItem menuItem;

    ViewPager viewPager;
    ListPagerAdapter pagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            if (pagerAdapter.canGoBack()) {
                pagerAdapter.goBack();
            } else {
                super.onBackPressed();
            }
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
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
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.test, menu);
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        menuItem = menu.findItem(R.id.menu_search);
        searchView = (SearchView) menuItem.getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        // This is created here as I need to pass the SearchView to the fragments in the pager.
        pagerAdapter = new ListPagerAdapter(searchView, getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
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

    public class ListPagerAdapter extends FragmentPagerAdapter {
        private static final String TAG = RMPDApplication.APP_PREFIX + "ListPagerAdapter";
        SearchView searchView;
        TestFragment firstFragment;
        PlaylistFragment playlistFragment;


        public ListPagerAdapter(SearchView searchView, FragmentManager fm) {
            super(fm);
            this.searchView = searchView;
        }

        public boolean canGoBack() {
            return firstFragment.canGoBack();
        }

        public void goBack() {
            firstFragment.goBack();
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 1) {
                Log.i(TAG, "Returning playlistFragment");
                if (playlistFragment == null) {
                    playlistFragment = new PlaylistFragment(searchView);
                }
                return playlistFragment;

            } else {
                Log.i(TAG, "Returning firstFragment");
                if (firstFragment == null) {
                    firstFragment = new TestFragment(searchView);
                }
                return firstFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}

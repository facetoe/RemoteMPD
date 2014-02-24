package com.facetoe.remotempd;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import com.facetoe.remotempd.fragments.ArtistListFragment;

public class TestActivity extends Activity {
    private static final String TAG = RMPDApplication.APP_PREFIX + "TestActivity";
    private final RMPDApplication app = RMPDApplication.getInstance();
    private boolean shouldShowFragment = true;
    SearchView searchView;
    MenuItem menuItem;
    ArtistListFragment listFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // If savedInstance state isn't null then showing the fragment will
        // result in overlapping fragments.
        if(savedInstanceState != null) {
            shouldShowFragment = false;
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
        searchView = (SearchView)menuItem.getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        // The reason for adding the fragment here is I need to be able to
        // pass the search view in to perform filtering on search.
        if(shouldShowFragment) {
            showListFragment(searchView);
        }
        return true;
    }

    private void showListFragment(SearchView searchView) {
        if (findViewById(R.id.filterableListContainer) != null) {

            // Create a new Fragment to be placed in the activity layout
            listFragment = new ArtistListFragment(searchView);

            // Add the fragment to the 'fragment_container' FrameLayout
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.filterableListContainer, listFragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.commit();
        } else {
            Log.e(TAG, "Couldn't find filterableListFragment");
        }
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
                Log.d(TAG, "Bluetooth enabled");
                app.notifyEvent(RMPDApplication.Event.CONNECT);
            } else {
                Log.d(TAG, "Bluetooth not enabled");
                app.notifyEvent(RMPDApplication.Event.REFUSED_BT_ENABLE);
            }
        }
    }
}

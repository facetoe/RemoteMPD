package com.facetoe.remotempd.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import com.facetoe.remotempd.R;
import com.facetoe.remotempd.RMPDApplication;
import com.facetoe.remotempd.adapters.AbstractMPDArrayAdapter;
import org.a0z.mpd.Item;
import org.a0z.mpd.MPD;
import org.a0z.mpd.MPDStatus;
import org.a0z.mpd.event.StatusChangeListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * RemoteMPD
 * Created by facetoe on 23/02/14.
 */
abstract class AbstractListFragment extends Fragment implements AdapterView.OnItemClickListener,
        SearchView.OnQueryTextListener {
    private static final String TAG = RMPDApplication.APP_PREFIX + "AbstractListFragment";
    protected final MPD mpd = RMPDApplication.getInstance().getMpd();
    protected AbstractMPDArrayAdapter adapter;
    protected ListView listItems;
    protected List entries = new ArrayList<Item>();
    protected SearchView searchView;

    public AbstractListFragment(SearchView searchView) {
        this.searchView = searchView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(!newText.isEmpty()) {
            Log.i(TAG, "Search changed: " + newText);
            adapter.getFilter().filter(newText);
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.i(TAG, "Search submitted: " + query);
        adapter.getFilter().filter(query);

        hideSoftKeyboard();
        hideSearchView();
        return true;
    }

    private void hideSearchView() {
        Activity parentActivity = getActivity();
        if (parentActivity != null) {
            parentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    searchView.setQuery("", false);
                    searchView.clearFocus();
                    searchView.setIconified(true);
                }
            });
        }
    }

    private void hideSoftKeyboard() {
        Activity parentActivity = getActivity();
        if (parentActivity != null) {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
        }
    }

    protected void updateEntries(final List<? extends Item> newItems) {
        Activity parentActivity = getActivity();
        if (parentActivity != null) {
             parentActivity.runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     adapter.resetEntries(newItems);
                 }
             });
        }
    }

    protected void replaceWithFragment(Fragment fragment) {
        // Hide these when swapping fragments as otherwise they
        // will hang around when the user clicks an item in the ListView
        hideSearchView();
        hideSoftKeyboard();

        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.filterableListContainer, fragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.addToBackStack(null);
            ft.commit();
        } else {
            Log.w(TAG, "Fragment manager was null in replaceWithFragment()");
        }
    }
}

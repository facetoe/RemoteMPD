package com.facetoe.remotempd.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.facetoe.remotempd.R;
import com.facetoe.remotempd.RMPDApplication;
import com.facetoe.remotempd.TestActivity;
import com.facetoe.remotempd.adapters.AbstractMPDArrayAdapter;
import org.a0z.mpd.Item;
import org.a0z.mpd.MPD;

import java.util.ArrayList;
import java.util.List;

/**
 * RemoteMPD
 * Created by facetoe on 23/02/14.
 */
public abstract class AbstractListFragment extends Fragment
        implements AdapterView.OnItemClickListener,
        SearchView.OnQueryTextListener,
        TestActivity.OnFragmentVisible {

    private static final String TAG = RMPDApplication.APP_PREFIX + "AbstractListFragment";

    final RMPDApplication app = RMPDApplication.getInstance();
    final MPD mpd = app.getMpd();

    final List<Item> entries = new ArrayList<Item>();
    AbstractMPDArrayAdapter adapter;
    LinearLayout spinnerLayout;
    private SearchView searchView;
    ListView listItems;

    abstract void initAdapter();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.filterable_list, container, false);
        assert rootView != null;
        spinnerLayout = (LinearLayout) rootView.findViewById(R.id.filterableListSpinnerLayout);
        searchView = (SearchView)rootView.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(this);

        listItems = (ListView) rootView.findViewById(R.id.listItems);
        TextView emptyListMessage = (TextView) rootView.findViewById(R.id.txtEmptyFilterableList);
        listItems.setEmptyView(emptyListMessage);
        listItems.setOnItemClickListener(this);

        initAdapter();
        listItems.setAdapter(adapter);

        registerForContextMenu(listItems);
        setRetainInstance(true);

        return rootView;
    }

    protected void setTitle(final String title) {
        final Activity parentActivity = getActivity();
        if (parentActivity != null) {
            parentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parentActivity.setTitle(title);
                }
            });
        } else {
            Log.w(TAG, "Can't set title, activity was null");
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Log.v(TAG, "TextChanged: " + newText);
        adapter.getFilter().filter(newText);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.v(TAG, "Search submitted: " + query);
        hideSoftKeyboard();
        hideSearchView();
        return true;
    }

    protected void hideKeyboardAndCollapseSearchView() {
        hideSearchView();
        hideSoftKeyboard();
    }

    private void hideSearchView() {
        Activity parentActivity = getActivity();
        if (parentActivity != null && searchView != null) {
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

    protected void showActionbarProgressSpinner() {
        final Activity parentActivity = getActivity();
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                parentActivity.setProgressBarIndeterminateVisibility(true);
            }
        });
    }

    protected void hideActionbarProgressSpinner() {
        final Activity parentActivity = getActivity();
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                parentActivity.setProgressBarIndeterminateVisibility(false);
            }
        });
    }

    void updateEntries(final List<? extends Item> newItems) {
        Log.d(TAG, "Updating " + newItems.size() + " entries");
        Activity parentActivity = getActivity();
        if (parentActivity != null) {
            parentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    entries.clear();
                    entries.addAll(newItems);
                    adapter.resetEntries(newItems);
                }
            });
        }
    }
}

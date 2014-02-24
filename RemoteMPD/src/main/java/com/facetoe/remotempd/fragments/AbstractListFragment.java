package com.facetoe.remotempd.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.facetoe.remotempd.R;
import com.facetoe.remotempd.RMPDApplication;
import com.facetoe.remotempd.adapters.AbstractMPDArrayAdapter;
import org.a0z.mpd.*;
import org.a0z.mpd.exception.MPDServerException;

import java.util.ArrayList;
import java.util.List;

/**
 * RemoteMPD
 * Created by facetoe on 23/02/14.
 */
abstract class AbstractListFragment extends Fragment implements AdapterView.OnItemClickListener,
        SearchView.OnQueryTextListener {
    private static final String TAG = RMPDApplication.APP_PREFIX + "AbstractListFragment";
    protected final RMPDApplication app = RMPDApplication.getInstance();
    protected final MPD mpd = app.getMpd();
    protected AbstractMPDArrayAdapter adapter;
    protected ListView listItems;
    protected List entries = new ArrayList<Item>();
    protected SearchView searchView;


    protected static final int ADD_ITEM = 1;
    protected static final int ADD_AND_REPLACE = 2;
    protected static final int ADD_REPLACE_AND_PLAY = 3;
    protected static final int ADD_AND_PLAY = 4;
    protected LinearLayout spinnerLayout;

    abstract protected AbstractMPDArrayAdapter getAdapter();

    abstract protected String getTitle();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.filterable_list, container, false);
        spinnerLayout = (LinearLayout) rootView.findViewById(R.id.filterableListSpinnerLayout);

        Activity parentActivity = getActivity();
        if (parentActivity != null) {
            parentActivity.setTitle(getTitle());
        } else {
            Log.w(TAG, "parentActivity was null");
        }

        listItems = (ListView) rootView.findViewById(R.id.listItems);
        TextView emptyMessage = (TextView) rootView.findViewById(R.id.txtEmptyFilterableList);
        listItems.setEmptyView(emptyMessage);
        listItems.setOnItemClickListener(this);
        registerForContextMenu(listItems);

        adapter = getAdapter();
        listItems.setAdapter(adapter);

        setRetainInstance(true);

        // Clear the filter if we are returning to this fragment
        // otherwise if the user filters, then clicks on an item, then clicks back
        // the list will still be showing the filtered results.
        if (savedInstanceState != null) {
            adapter.getFilter().filter("");
        }

        return rootView;
    }

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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.listItems) {
            ListView lv = (ListView) v;
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Item item = (Item) lv.getItemAtPosition(acmi.position);

            menu.add(Menu.NONE, ADD_ITEM, Menu.NONE, "Add " + getItemName(item));
            menu.add(Menu.NONE, ADD_AND_REPLACE, Menu.NONE, "Add and replace " + getItemName(item));
            menu.add(Menu.NONE, ADD_REPLACE_AND_PLAY, Menu.NONE, "Add replace and play");
            menu.add(Menu.NONE, ADD_AND_PLAY, Menu.NONE, "Add and play");
        }
    }

    private String getItemName(Item item) {
        if (item instanceof Artist) {
            return "artist";
        } else if (item instanceof Album) {
            return "album";
        } else if (item instanceof Music) {
            return "song";
        } else {
            Log.e(TAG, "Unknown item passed to getItemName()");
            return "Unknown";
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null) {
            Log.e(TAG, "AdapterContextMenuInfo was null");
            return true;
        }

        Item selectedItem = adapter.getItem(info.position);
        switch (item.getItemId()) {
            case ADD_ITEM:
                Log.d(TAG, "Add clicked: " + selectedItem);
                addItem(selectedItem, false, false);
                break;

            case ADD_AND_REPLACE:
                Log.d(TAG, "Add and replace clicked: " + selectedItem);
                addItem(selectedItem, true, false);
                break;

            case ADD_REPLACE_AND_PLAY:
                Log.d(TAG, "Add, replace and play clicked: " + selectedItem);
                addItem(selectedItem, true, true);
                break;

            case ADD_AND_PLAY:
                Log.d(TAG, "Add and play clicked: " + selectedItem);
                addItem(selectedItem, false, true);
                break;

            default:
                Log.i(TAG, "Unknown: " + item.getItemId());
                break;
        }
        return true;
    }

    private void addItem(Item item, boolean replace, boolean play) {
        if (item instanceof Artist) {
            add((Artist) item, replace, play);
        } else if (item instanceof Album) {
            add((Album) item, replace, play);
        } else if (item instanceof Music) {
            add((Music) item, replace, play);
        } else {
            Log.w(TAG, "Unknown item passed to addItem()");
        }
    }

    private void add(final Artist artist, final boolean replace, final boolean play) {
        final Toast toast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mpd.add(artist, replace, play);
                    toast.setText("Added " + artist.getName());
                    toast.show();
                } catch (MPDServerException e) {
                    app.notifyEvent(RMPDApplication.Event.CONNECTION_FAILED);
                }
            }
        }).start();
    }

    private void add(final Album album, final boolean replace, final boolean play) {
        final Toast toast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mpd.add(album, replace, play);
                    toast.setText("Added " + album.getName());
                    toast.show();
                } catch (MPDServerException e) {
                    app.notifyEvent(RMPDApplication.Event.CONNECTION_FAILED);
                }
            }
        }).start();
    }

    private void add(final Music song, final boolean replace, final boolean play) {
        final Toast toast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mpd.add(song, replace, play);
                    toast.setText("Added " + song.getName());
                    toast.show();
                } catch (MPDServerException e) {
                    app.notifyEvent(RMPDApplication.Event.CONNECTION_FAILED);
                }
            }
        }).start();
    }

    @Override
    public boolean onQueryTextChange(String newText) {
//        if (!newText.isEmpty()) {
//            Log.i(TAG, "Search changed: " + newText);
//        }
        Log.i(TAG, "textChanged: " + newText);
        adapter.getFilter().filter(newText);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.i(TAG, "Search submitted: " + query);
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
                    entries.clear();
                    entries.addAll(newItems);
                    adapter.resetEntries(newItems);
                }
            });
        }
    }

    protected void replaceWithFragment(Fragment fragment) {
        // Hide these when swapping fragments as otherwise they
        // will hang around when the user clicks an item in the ListView
        // while still in the process of searching
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

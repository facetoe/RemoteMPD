package com.facetoe.remotempd.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.facetoe.remotempd.R;
import com.facetoe.remotempd.RMPDApplication;
import com.facetoe.remotempd.adapters.AbstractMPDArrayAdapter;
import com.facetoe.remotempd.adapters.ItemAdapter;
import org.a0z.mpd.*;
import org.a0z.mpd.exception.MPDServerException;

import java.util.ArrayList;
import java.util.List;

/**
 * RemoteMPD
 * Created by facetoe on 23/02/14.
 */
public abstract class AbstractListFragment extends Fragment
        implements AdapterView.OnItemClickListener,
        SearchView.OnQueryTextListener {
    private static final String TAG = RMPDApplication.APP_PREFIX + "AbstractListFragment";
    final RMPDApplication app = RMPDApplication.getInstance();
    final MPD mpd = app.getMpd();

    final List<Item> entries = new ArrayList<Item>();
    ItemAdapter adapter;
    final SearchView searchView;

    private static final int ADD_ITEM = 1;
    private static final int ADD_AND_REPLACE = 2;
    private static final int ADD_REPLACE_AND_PLAY = 3;
    private static final int ADD_AND_PLAY = 4;
    LinearLayout spinnerLayout;
    ListView listItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.filterable_list, container, false);
        assert rootView != null;
        spinnerLayout = (LinearLayout) rootView.findViewById(R.id.filterableListSpinnerLayout);

        setTitle();

        listItems = (ListView) rootView.findViewById(R.id.listItems);
        TextView emptyListMessage = (TextView) rootView.findViewById(R.id.txtEmptyFilterableList);
        listItems.setEmptyView(emptyListMessage);
        listItems.setOnItemClickListener(this);
        registerForContextMenu(listItems);

        adapter = new ItemAdapter(getActivity(), R.layout.song_list, entries);
        listItems.setAdapter(adapter);

        setRetainInstance(true);

        // Clear the filter if we are returning to this fragment.
        // Otherwise if the user filters, then clicks on an item, then clicks back
        // the list will still be showing the filtered results.
        if (savedInstanceState != null) {
            adapter.getFilter().filter("");
        }

        return rootView;
    }

    private void setTitle() {
        Activity parentActivity = getActivity();
        if (parentActivity != null) {
            parentActivity.setTitle("Test");
        } else {
            Log.w(TAG, "parentActivity was null");
        }
    }

    AbstractListFragment(SearchView searchView) {
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
                Log.d(TAG, "Add: " + selectedItem);
                addItem(selectedItem, false, false);
                break;

            case ADD_AND_REPLACE:
                Log.d(TAG, "Add and replace: " + selectedItem);
                addItem(selectedItem, true, false);
                break;

            case ADD_REPLACE_AND_PLAY:
                Log.d(TAG, "Add, replace and play: " + selectedItem);
                addItem(selectedItem, true, true);
                break;

            case ADD_AND_PLAY:
                Log.d(TAG, "Add and play: " + selectedItem);
                addItem(selectedItem, false, true);
                break;

            default:
                Log.w(TAG, "Unknown: " + item.getItemId());
                break;
        }
        return true;
    }

    protected void addItem(Item item, boolean replace, boolean play) {
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

    protected void add(final Artist artist, final boolean replace, final boolean play) {
        final Toast toast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);

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

    protected void add(final Album album, final boolean replace, final boolean play) {
        final Toast toast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);

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

    protected void add(final Music song, final boolean replace, final boolean play) {
        final Toast toast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);

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
        Log.i(TAG, "TextChanged: " + newText);
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

    void updateEntries(final List<? extends Item> newItems) {
        Log.d(TAG, "Updating " + newItems.size() + " entries");
        Activity parentActivity = getActivity();
        if (parentActivity != null) {
            parentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    entries.clear();
                    //noinspection unchecked
                    entries.addAll(newItems);
                    //noinspection unchecked
                    adapter.resetEntries(newItems);
                }
            });
        }
    }
}

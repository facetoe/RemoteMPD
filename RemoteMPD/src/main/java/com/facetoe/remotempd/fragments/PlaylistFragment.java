package com.facetoe.remotempd.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.facetoe.remotempd.R;
import com.facetoe.remotempd.RMPDApplication;
import com.facetoe.remotempd.adapters.AbstractMPDArrayAdapter;
import com.facetoe.remotempd.adapters.PlaylistAdapter;
import org.a0z.mpd.*;
import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.exception.MPDServerException;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * RemoteMPD
 * Created by facetoe on 11/02/14.
 */
public class PlaylistFragment extends AbstractListFragment implements StatusChangeListener {
    private final String TAG = RMPDApplication.APP_PREFIX + "PlaylistFragment";

    private MPDStatus status;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    void initAdapter() {
        adapter = new PlaylistAdapter(getActivity(), entries);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        listItems.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listItems.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            ArrayList<Music> selectedItems = new ArrayList<Music>();

            @Override
            public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {
                Item item = adapter.getItem(position);
                if (checked) {
                    selectedItems.add((Music) item);

                } else if (selectedItems.contains(item)) {
                    selectedItems.remove(item);
                }
            }

            @Override
            public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                assert inflater != null;
                inflater.inflate(R.menu.contextual_action_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_delete:
                        removeSelectedSongsFromPlaylist();
                        mode.finish();
                        break;

                    default:
                        Log.i(TAG, "default");
                        break;

                }
                return false;
            }

            private void removeSelectedSongsFromPlaylist() {
                int numSelectedItems = selectedItems.size();
                final int[] songIds = new int[numSelectedItems];
                for (int i = 0; i < numSelectedItems; i++) {
                    songIds[i] = selectedItems.get(i).getSongId();
                }
                Log.i(TAG, "Deleting: " + selectedItems + " ids: " + Arrays.toString(songIds));
                selectedItems.clear();
                removeSongs(songIds);
            }

            private void removeSongs(final int[] songIds) {
                final Toast toast = Toast.makeText(getActivity(), "Removed " + songIds.length + " songs", Toast.LENGTH_SHORT);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mpd.getPlaylist().removeById(songIds);
                            toast.show();
                        } catch (MPDServerException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                }).start();
            }

            @Override
            public void onDestroyActionMode(android.view.ActionMode mode) {

            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        app.getAsyncHelper().addStatusChangeListener(this);
        if (entries.size() == 0) {
            updateEntries(app.getMpd().getPlaylist().getMusicList());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            onVisible();
        }
    }

    @Override
    public void onVisible() {
        setTitle("Playlist");
        if(status != null) {
            setPlayingIcon(status);
            listItems.post(new Runnable() {
                @Override
                public void run() {
                    listItems.setSelection(status.getSongPos());
                }
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        app.getAsyncHelper().removeStatusChangeListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.playlist_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_playlist:
                clearPlaylist();
                return true;
        }
        return false;
    }

    private void clearPlaylist() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Confirm");
        builder.setMessage("Clear Playlist?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            app.getMpd().getPlaylist().clear();
                        } catch (MPDServerException e) {
                            handleError(e);
                        }
                    }
                }).start();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void handleError(MPDServerException e) {
        app.notifyEvent(RMPDApplication.Event.CONNECTION_FAILED, e.getMessage());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Music song = (Music) adapter.getItem(position);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mpd.skipToId(song.getSongId());
                } catch (MPDServerException e) {
                    handleError(e);
                }
            }
        }).start();
    }

    @Override
    public void volumeChanged(MPDStatus mpdStatus, int oldVolume) {
        status = mpdStatus;
    }

    @Override
    public void playlistChanged(MPDStatus mpdStatus, int oldPlaylistVersion) {
        status = mpdStatus;
        updateEntries(app.getMpd().getPlaylist().getMusicList());
    }

    @Override
    public void trackChanged(MPDStatus mpdStatus, int oldTrack) {
        setPlayingIcon(mpdStatus);
    }

    private void setPlayingIcon(MPDStatus mpdStatus) {
        PlaylistAdapter playlistAdapter = (PlaylistAdapter)adapter;
        playlistAdapter.setNowPlayingIcon(mpdStatus.getSongId());
    }

    @Override
    public void stateChanged(MPDStatus mpdStatus, String oldState) {
        status = mpdStatus;

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
}

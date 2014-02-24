package com.facetoe.remotempd.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.facetoe.remotempd.R;
import com.facetoe.remotempd.RMPDApplication;
import com.facetoe.remotempd.adapters.FilterTextWatcher;
import com.facetoe.remotempd.adapters.SongListAdapter;
import org.a0z.mpd.Album;
import org.a0z.mpd.Music;
import org.a0z.mpd.exception.MPDServerException;

import java.util.List;

/**
 * RemoteMPD
 * Created by facetoe on 23/02/14.
 */
public class SongListFragment extends AbstractListFragment {
    private static final String TAG = RMPDApplication.APP_PREFIX + "SongListFragment";
    private Album album;
    private LinearLayout spinnerLayout;

    SongListFragment(SearchView searchView, Album album) {
        super(searchView);
        this.album = album;
    }

    @Override
    public void onStart() {
        super.onStart();
        new LoadAlbumSongsTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.filterable_list, container, false);
        spinnerLayout = (LinearLayout)rootView.findViewById(R.id.filterableListSpinnerLayout);

        adapter = new SongListAdapter(getActivity(), R.layout.song_list, entries);

        getActivity().setTitle(album.getName());

        listItems = (ListView) rootView.findViewById(R.id.listItems);
        TextView emptyMessage = (TextView)rootView.findViewById(R.id.txtEmptyFilterableList);
        listItems.setEmptyView(emptyMessage);

        listItems.setAdapter(adapter);
        listItems.setOnItemClickListener(this);
        registerForContextMenu(listItems);
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Music song = (Music)adapter.getItem(position);
        final Toast toast = Toast.makeText(getActivity(), song.getTitle() + " added to playlist", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mpd.add(song);
                    toast.show();
                } catch (MPDServerException e) {
                    Log.e(TAG, "Error: " + e.getMessage());
                }
            }
        }).start();
    }

    class LoadAlbumSongsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            spinnerLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            spinnerLayout.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                entries.addAll(mpd.getSongs(album.getArtist(), album));
                updateEntries(entries);
            } catch (MPDServerException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
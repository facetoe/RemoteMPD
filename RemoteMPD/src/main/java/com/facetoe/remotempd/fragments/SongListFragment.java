package com.facetoe.remotempd.fragments;

import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SearchView;
import android.widget.Toast;
import com.facetoe.remotempd.R;
import com.facetoe.remotempd.RMPDApplication;
import com.facetoe.remotempd.adapters.AbstractMPDArrayAdapter;
import com.facetoe.remotempd.adapters.SongListAdapter;
import org.a0z.mpd.Album;
import org.a0z.mpd.Music;
import org.a0z.mpd.exception.MPDServerException;

/**
 * RemoteMPD
 * Created by facetoe on 23/02/14.
 */
public class SongListFragment extends AbstractListFragment {
    private final Album album;

    SongListFragment(SearchView searchView, Album album) {
        super(searchView);
        this.album = album;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (entries.size() == 0) {
            new LoadAlbumSongsTask().execute();
        }
    }

    @Override
    protected AbstractMPDArrayAdapter getAdapter() {
        //noinspection unchecked
        return new SongListAdapter(getActivity(), R.layout.song_list, entries);
    }

    @Override
    protected String getTitle() {
        return album.getName();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Music song = (Music) adapter.getItem(position);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mpd.add(song);
                    Toast toast = makeToast(song.getTitle() + " added to playlist");
                    toast.show();
                } catch (MPDServerException e) {
                    app.notifyEvent(RMPDApplication.Event.CONNECTION_FAILED);
                }
            }
        }).start();
    }

    private class LoadAlbumSongsTask extends AsyncTask<Void, Void, Void> {

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
                updateEntries(mpd.getSongs(album.getArtist(), album));
            } catch (MPDServerException e) {
                app.notifyEvent(RMPDApplication.Event.CONNECTION_FAILED);
            }
            return null;
        }
    }
}
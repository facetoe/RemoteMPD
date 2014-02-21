package com.facetoe.remotempd.adapters;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import com.facetoe.remotempd.RMPDApplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RemoteMPD
 * Created by facetoe on 21/02/14.
 *
 * Base class for MPDArray adapters. Provides filtering.
 */
public class AbstractMPDArrayAdapter<T> extends ArrayAdapter<T> {
    protected Context context;
    protected List<T> items;
    protected List<T> storedItems;
    protected int itemLayoutID;
    protected String TAG = RMPDApplication.APP_PREFIX + "AbstractMPDArrayAdapter";

    AbstractMPDArrayAdapter(Context context, int itemLayoutID, List<T> items) {
        super(context, itemLayoutID, items);
        this.context = context;
        this.itemLayoutID = itemLayoutID;
        this.items = items;
        this.storedItems = new ArrayList<T>(items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        throw new UnsupportedOperationException("You must implement getView()!");
    }

    @Override
    public T getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    public void resetEntries(List<T> newItems) {
        storedItems.clear();
        storedItems.addAll(newItems);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                if (constraint == null || constraint.length() == 0) {
                    // No filter implemented we return all the list
                    results.values = storedItems;
                    results.count = storedItems.size();
                } else {
                    List<T> entries = new ArrayList<T>();

                    for (T entry : storedItems) {
                        if (entry.toString().toUpperCase().startsWith(constraint.toString().toUpperCase()))
                            entries.add(entry);
                    }

                    Log.i(TAG, "Found " + entries.size() + " matches for " + constraint);

                    results.values = entries;
                    results.count = entries.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.count == 0) {
                    Log.i(TAG, "No results");
                    items = Collections.emptyList();
                    notifyDataSetChanged();
                } else {
                    Log.i(TAG, "Got " + results.count + " results");
                    items = (List<T>) results.values;
                    notifyDataSetChanged();
                }
            }
        };
    }
}

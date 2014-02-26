package com.facetoe.remotempd.adapters;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import com.facetoe.remotempd.RMPDApplication;
import org.a0z.mpd.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RemoteMPD
 * Created by facetoe on 21/02/14.
 *
 * Base class for MPDArray adapters. Provides filtering.
 */
public class AbstractMPDArrayAdapter extends ArrayAdapter<Item> {
    protected Context context;
    protected List<Item> items;
    protected List<Item> storedItems;
    protected int itemLayoutID;
    protected String TAG = RMPDApplication.APP_PREFIX + "AbstractMPDArrayAdapter";

    public AbstractMPDArrayAdapter(Context context, int itemLayoutID, List<Item> items) {
        super(context, itemLayoutID, items);
        this.context = context;
        this.itemLayoutID = itemLayoutID;
        this.items = items;
        this.storedItems = new ArrayList<Item>(items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        throw new UnsupportedOperationException("You must implement getView()!");
    }

    @Override
    public Item getItem(int position) {
        if(items.size() == 0 || position >= items.size()) {
            Log.e(TAG, "Invalid position. Expected <= " + items.size() + " got " + position);
            return null;
        } else {
            return items.get(position);
        }
    }

    @Override
    public int getCount() {
        return items.size();
    }

    public void resetEntries(List<? extends Item> newItems) {
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
                Log.i(TAG, "performFiltering");
                if (constraint == null || constraint.length() == 0) {
                    // No filter implemented we return all the list
                    results.values = storedItems;
                    results.count = storedItems.size();
                } else {
                    List<Item> matches = filterMatches(constraint);
                    results.values = matches;
                    results.count = matches.size();
                }
                return results;
            }

            private List<Item> filterMatches(CharSequence constraint) {
                List<Item> matches = new ArrayList<Item>();
                for (Item item : storedItems) {
                    if (item.toString().toUpperCase().startsWith(constraint.toString().toUpperCase()))
                        matches.add(item);
                }
                return matches;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.count == 0) {
                    Log.d(TAG, "No results: " + items);
                    items = Collections.emptyList();
                } else {
                    Log.d(TAG, "Got " + results.count + " results");
                    items = (List<Item>) results.values;
                }
                notifyDataSetChanged();
            }
        };
    }
}

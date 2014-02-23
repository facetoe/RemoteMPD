package com.facetoe.remotempd.adapters;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * RemoteMPD
 * Created by facetoe on 22/02/14.
 */
public class FilterTextWatcher implements TextWatcher {
    AbstractMPDArrayAdapter adapter;
    public FilterTextWatcher(AbstractMPDArrayAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        adapter.getFilter().filter(s);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
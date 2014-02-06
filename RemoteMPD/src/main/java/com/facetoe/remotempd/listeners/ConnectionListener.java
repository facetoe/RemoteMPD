package com.facetoe.remotempd.listeners;

/**
 * RemoteMPD
 * Created by facetoe on 6/02/14.
 */
// Listener to report changes in the MPDConnection state.
public interface ConnectionListener {
    public void connectionFailed(String message);

    public void connectionSucceeded(String message);
}

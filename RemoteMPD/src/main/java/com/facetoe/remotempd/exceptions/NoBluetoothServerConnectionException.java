package com.facetoe.remotempd.exceptions;

import org.a0z.mpd.exception.MPDException;

/**
 * RemoteMPD
 * Created by facetoe on 3/02/14.
 */
public class NoBluetoothServerConnectionException extends MPDException {
    public NoBluetoothServerConnectionException(String detailMessage) {
        super(detailMessage);
    }
}

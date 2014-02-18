package com.facetoe.remotempd.exceptions;

import org.a0z.mpd.exception.MPDException;
import org.a0z.mpd.exception.MPDServerException;

/**
 * RemoteMPD
 * Created by facetoe on 3/02/14.
 */
public class NoBluetoothServerConnectionException extends MPDServerException {
    public NoBluetoothServerConnectionException(String detailMessage) {
        super(detailMessage);
    }
}

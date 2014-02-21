package org.a0z.mpd.exception;

/**
 * RemoteMPD
 * Created by facetoe on 3/02/14.
 */
public class NoBluetoothServerConnectionException extends MPDServerException {
    public NoBluetoothServerConnectionException(String detailMessage) {
        super(detailMessage);
    }
}

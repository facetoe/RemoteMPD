package org.a0z.mpd;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import org.a0z.mpd.exception.MPDException;

/**
 * dmixUnmodified
 * Created by facetoe on 20/02/14.
 */
public class Test {

    public static void main(String[] args) {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = btAdapter.getRemoteDevice("74:E5:43:D5:8B");
        try {
            MPD mpd = new MPD(device);
        } catch (MPDException e) {
            e.printStackTrace();
        }
    }
}

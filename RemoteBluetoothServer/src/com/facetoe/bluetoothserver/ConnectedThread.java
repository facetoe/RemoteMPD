package com.facetoe.bluetoothserver;

import org.a0z.mpdlocal.*;
import org.a0z.mpdlocal.event.StatusChangeListener;
import org.a0z.mpdlocal.event.TrackPositionListener;
import org.a0z.mpdlocal.exception.MPDServerException;

import java.io.IOException;

public class ConnectedThread implements Runnable {
    MPDManager mpdManager;
    public ConnectedThread(MPDManager mpdManager) {
        System.out.println("Entered ConnectedThread: " + Thread.currentThread().getName());
        this.mpdManager = mpdManager;
    }

    @Override
    public void run() {
        try {
            mpdManager.run();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MPDServerException e) {
            e.printStackTrace();
        }
    }
}


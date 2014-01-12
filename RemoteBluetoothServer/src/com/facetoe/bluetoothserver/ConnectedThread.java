package com.facetoe.bluetoothserver;

import org.a0z.mpdlocal.*;
import org.a0z.mpdlocal.event.StatusChangeListener;
import org.a0z.mpdlocal.event.TrackPositionListener;
import org.a0z.mpdlocal.exception.MPDServerException;

import javax.microedition.io.StreamConnection;
import java.io.IOException;

public class ConnectedThread extends Thread {
    StreamConnection connection;
    public ConnectedThread(StreamConnection connection) {
        System.out.println("Entered ConnectedThread: " + Thread.currentThread().getName());
        this.connection = connection;
    }

    @Override
    public void run() {
        try {
            String host = "localhost";
            String passwd = "password";
            int port = 6600;
            MPDManager manager = new MPDManager();
            manager.setPasswd(passwd);
            manager.setPort(port);
            manager.setHost(host);
            manager.setConnection(connection);
            manager.run();
            connection.close();
            System.out.println("Run finished");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MPDServerException e) {
            e.printStackTrace();
        }
    }
}


package com.facetoe.bluetoothserver;


import org.a0z.mpdlocal.exception.MPDServerException;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.IOException;

public class WaitThread implements Runnable {
    /**
     * Constructor
     */
    public WaitThread() {
    }

    @Override
    public void run() {
        waitForConnection();
        Thread.currentThread().interrupt();
    }

    /**
     * Waiting for connection from devices
     */
    private void waitForConnection() {

        // retrieve the local Bluetooth device object
        LocalDevice local = null;

        StreamConnectionNotifier notifier;
        StreamConnection connection = null;

        // setup the server to listen for connection
        try {
            local = LocalDevice.getLocalDevice();
            local.setDiscoverable(DiscoveryAgent.GIAC);
            UUID uuid = new UUID("04c6093b00001000800000805f9b34fb", false);
            System.out.println(uuid.toString());
            String url = "btspp://localhost:" + uuid.toString() + ";name=RemoteBluetooth";
            notifier = (StreamConnectionNotifier) Connector.open(url);
        } catch (BluetoothStateException e) {
            System.err.println("Bluetooth is not turned on.");
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }


        // waiting for connection
        Thread thread;
        while (true) {
            try {
                System.out.println("waiting for connection...");
                connection = notifier.acceptAndOpen();
                thread = new ConnectedThread(connection);
                thread.start();
                System.out.println("Before join");
                thread.join();
                System.out.println("After join");

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

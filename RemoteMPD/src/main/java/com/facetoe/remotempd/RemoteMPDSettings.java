package com.facetoe.remotempd;

/**
 * Created by facetoe on 7/01/14.
 */
public class RemoteMPDSettings {
    private String host = "";
    private int port;
    private String password = "";
    private boolean isBluetooth;
    private String lastDevice = "";

    public RemoteMPDSettings() {
    }

    public RemoteMPDSettings(String host, String port, String password, String lastDevice, boolean isBluetooth) {
        this.host = host;
        this.port = Integer.parseInt(port);
        this.password = password;
        this.lastDevice = lastDevice;
        this.isBluetooth = isBluetooth;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isBluetooth() {
        return isBluetooth;
    }

    public void setBluetooth(boolean isBluetooth) {
        this.isBluetooth = isBluetooth;
    }

    public String getLastDevice() {
        return lastDevice;
    }

    public void setLastDevice(String lastDevice) {
        this.lastDevice = lastDevice;
    }

    @Override
    public String toString() {
        return "\nRemoteMPDSettings{" +
                "\nhost='" + host + '\'' +
                "\nport=" + port +
                "\npassword='" + password + '\'' +
                "\nisBluetooth=" + isBluetooth +
                "\nlastDevice='" + lastDevice + '\'' +
                '}';
    }
}

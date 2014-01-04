package com.facetoe.bluetoothserver;

import com.google.gson.Gson;
import org.a0z.mpdlocal.*;
import org.a0z.mpdlocal.event.StatusChangeListener;
import org.a0z.mpdlocal.event.TrackPositionListener;
import org.a0z.mpdlocal.exception.MPDServerException;

import javax.microedition.io.StreamConnection;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by facetoe on 31/12/13.
 */
public class MPDManager implements TrackPositionListener, StatusChangeListener {
    private boolean DEBUG = true;
    private MPD mpdComm;
    private Gson gson = new Gson();
    private StreamConnection connection;
    private BufferedInputStream inputStream;
    private BufferedOutputStream outputStream;
    private StringBuilder sb = new StringBuilder(1024);
    private static final Pattern digit = Pattern.compile("\\d+");
    private boolean isCanceled = false;
    private String host;
    private int port;
    private String passwd;
    private MPDStatusMonitor statusMonitor;

    public MPDManager(MPD mpdComm) {
        this.mpdComm = mpdComm;
        statusMonitor = new MPDStatusMonitor(mpdComm, 1000);
        statusMonitor.addStatusChangeListener(this);
    }

    private void initConnection() throws MPDServerException, IOException {
        if (!mpdComm.isConnected())
            mpdComm.connect(host, port, passwd);

        if (!statusMonitor.isAlive())
            statusMonitor.start();

        if (statusMonitor.isPaused())
            statusMonitor.setPaused(false);

        this.inputStream = new BufferedInputStream(connection.openInputStream(), 1024);
        this.outputStream = new BufferedOutputStream(connection.openOutputStream(), 1024);
    }

    public void connect(String server, int port, String password) throws MPDServerException, UnknownHostException {
        mpdComm.connect(server, port, password);
    }

    public boolean isConnected() {
        return mpdComm.isConnected();
    }

    public void run() throws IOException, MPDServerException {
        initConnection();
        if (DEBUG) System.out.println("Running");
        int ch;
        while (!isCanceled) {
            if (DEBUG) System.out.println("Waiting for input...");
            while ((ch = inputStream.read()) != 10) {
                if (ch == -1) {
                    isCanceled = true;
                    break;
                } else {
                    sb.append((char) ch);
                }
            }

            if (!isCanceled) {
                if (DEBUG) System.out.println(sb);
                processCommand(sb.toString());
                sb.setLength(0);
            }
        }
        closeConnection();
    }

    private void processCommand(final String command) throws MPDServerException {
        if (command.equals(MPDCommand.MPD_CMD_PLAY)) mpdComm.play();
        else if (command.equals(MPDCommand.MPD_CMD_NEXT)) mpdComm.next();
        else if (command.equals(MPDCommand.MPD_CMD_PREV)) mpdComm.previous();
        else if (command.equals(MPDCommand.MPD_CMD_VOLUME)) mpdComm.adjustVolume(extractInt(command));
        else if (command.startsWith(MPDCommand.MPD_CMD_PLAY_ID)) mpdComm.skipToId(extractInt(command));
        else if (command.startsWith(MPDCommand.MPD_CMD_PLAYLIST_CHANGES)) updatePlaylist();
        else if (DEBUG) System.out.println("Unknown command: " + command);
    }

    private void closeConnection() throws IOException {
        inputStream.close();
        outputStream.close();
        connection.close();
        statusMonitor.setPaused(true);
        if(DEBUG) System.out.println("Connection closed.");
    }

    private int extractInt(String str) {
        Matcher m = digit.matcher(str);
        if (m.find()) return Integer.parseInt(m.group());
        else return -1;
    }

    public void reset() {
        isCanceled = false;
    }

    private void write(Object obj) {
        write(gson.toJson(obj));
    }

    private void write(String message) {
        try {
            int bytes = message.getBytes().length;
            write((message + "\n").getBytes("UTF-8"));
            if (DEBUG) System.out.println("Wrote: " + bytes + " bytes.");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(byte[] bytes) throws IOException {
        outputStream.write(bytes);
        outputStream.flush();
    }

    @Override
    public void volumeChanged(MPDStatus mpdStatus, int oldVolume) {
        if (DEBUG) System.out.println("Volume changed");
        write(new MPDResponse(MPDResponse.EVENT_VOLUME, mpdStatus, oldVolume));
    }

    @Override
    public void playlistChanged(MPDStatus mpdStatus, int oldPlaylistVersion) {
        if (DEBUG) System.out.println("Playlist changed");
        updatePlaylist();
    }

    // If you don't syncronize it you get a ConcurrentModification exception if it's updating when something changes.
    private synchronized void updatePlaylist() {
        if (DEBUG) System.out.println("Refreshing playlist");
        mpdComm.getPlaylist().playlistChanged(null, -1);
        MPDPlaylist playlist = mpdComm.getPlaylist();
        write(new MPDResponse(MPDResponse.EVENT_UPDATE_PLAYLIST, playlist.getMusicList()));
        if (DEBUG) System.out.println("Done");
    }

    @Override
    public void trackChanged(MPDStatus mpdStatus, int oldTrack) {
        if (DEBUG) System.out.println("Track changed");
        write(new MPDResponse(MPDResponse.EVENT_TRACK, mpdStatus, oldTrack));
    }

    @Override
    public void stateChanged(MPDStatus mpdStatus, String oldState) {
        if (DEBUG) System.out.println("State changed");
        write(new MPDResponse(MPDResponse.EVENT_STATE, mpdStatus, oldState));
    }

    @Override
    public void repeatChanged(boolean repeating) {
        if (DEBUG) System.out.println("Repeat changed");
        write(new MPDResponse(MPDResponse.EVENT_REPEAT, repeating));
    }

    @Override
    public void randomChanged(boolean random) {
        if (DEBUG) System.out.println("Random changed");
        write(new MPDResponse(MPDResponse.EVENT_RANDOM, random));
    }

    @Override
    public void connectionStateChanged(boolean connected, boolean connectionLost) {
        if (DEBUG) System.out.println("Connection State changed");
        write(new MPDResponse(MPDResponse.EVENT_CONNECTIONSTATE, connected, connectionLost));
    }

    @Override
    public void libraryStateChanged(boolean updating) {
        if (DEBUG) System.out.println("Library state changed");
        write(new MPDResponse(MPDResponse.EVENT_UPDATESTATE, updating));
    }

    @Override
    public void trackPositionChanged(MPDStatus status) {
        if (DEBUG) System.out.println("Track position changed");
        write(new MPDResponse(MPDResponse.EVENT_TRACKPOSITION, status));
    }

    public void setConnection(StreamConnection connection) {
        this.connection = connection;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }
}

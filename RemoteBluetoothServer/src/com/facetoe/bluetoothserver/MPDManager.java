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
    MPD mpdComm;
    MPD mpdMon;
    Gson gson = new Gson();
    StreamConnection connection;
    BufferedInputStream inputStream;
    BufferedOutputStream outputStream;
    StringBuilder sb = new StringBuilder(1024);
    private static final Pattern digit = Pattern.compile("\\d+");
    boolean isCanceled = false;
    private String host;
    private int port;
    private String passwd;
    private MPDTrackPositionMonitor trackPositionMonitor;
    private MPDStatusMonitor statusMonitor;

    public MPDManager(MPD mpdComm, MPD mpdMon) {
        this.mpdComm = mpdComm;
        this.mpdMon = mpdMon;
        statusMonitor = new MPDStatusMonitor(mpdMon, 1000);
        statusMonitor.addStatusChangeListener(this);
        trackPositionMonitor = new MPDTrackPositionMonitor(mpdMon, 1000);
        trackPositionMonitor.addTrackPositionListener(this);
    }

    private void initConnection() throws MPDServerException, IOException {
        if (!mpdComm.isConnected())
            mpdComm.connect(host, port, passwd);

        if (!mpdMon.isConnected())
            mpdMon.connect(host, port, passwd);

        if(!trackPositionMonitor.isAlive())
            trackPositionMonitor.start();

        if(trackPositionMonitor.isPaused())
            trackPositionMonitor.setPaused(false);

        if(!statusMonitor.isAlive())
            statusMonitor.start();

        if(statusMonitor.isPaused())
            statusMonitor.setPaused(false);

        this.inputStream = new BufferedInputStream(connection.openInputStream(), 1024);
        this.outputStream = new BufferedOutputStream(connection.openOutputStream(), 1024);
    }

    public void connect(String server, int port, String password) throws MPDServerException, UnknownHostException {
        mpdComm.connect(server, port, password);
        mpdMon.connect(server, port, password);
    }

    public boolean isConnected() {
        return mpdComm.isConnected() && mpdMon.isConnected();
    }

    public void run() throws IOException, MPDServerException {
        initConnection();
        System.out.println("Running");
        int ch;
        while (!isCanceled) {
            System.out.println("Waiting for input...");
            while ((ch = inputStream.read()) != 10) {
                if (ch == -1) {
                    isCanceled = true;
                    break;
                } else {
                    sb.append((char) ch);
                }
            }

            if (!isCanceled) {
                System.out.println(sb);
                processCommand(sb.toString());
                sb.setLength(0);
            }
        }
        closeConnection();
    }

    private void processCommand(String command) throws MPDServerException {
        if (command.equals(MPDCommand.MPD_CMD_PLAY)) mpdComm.play();
        else if (command.equals(MPDCommand.MPD_CMD_NEXT)) mpdComm.next();
        else if (command.equals(MPDCommand.MPD_CMD_PREV)) mpdComm.previous();
        else if (command.equals(MPDCommand.MPD_CMD_VOLUME)) mpdComm.adjustVolume(extractInt(command));
        else if (command.startsWith(MPDCommand.MPD_CMD_PLAY_ID)) mpdComm.skipToId(extractInt(command));
        else if(command.startsWith(MPDCommand.MPD_CMD_PLAYLIST_CHANGES)) updatePlaylist();
        else System.out.println("Unknown command: " + command);
    }

    private void closeConnection() throws IOException {
        inputStream.close();
        outputStream.close();
        connection.close();
        trackPositionMonitor.setPaused(true);
        statusMonitor.setPaused(true);
    }

    private int extractInt(String str) {
        Matcher m = digit.matcher(str);
        if (m.find()) return Integer.parseInt(m.group());
        else return -1;
    }

    public void cancel() throws IOException {
        isCanceled = true;
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
            System.out.println("Wrote: " + bytes + " bytes.");
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
        System.out.println("Volume changed");
    }

    @Override
    public void playlistChanged(MPDStatus mpdStatus, int oldPlaylistVersion) {
        System.out.println("Playlist changed");
        updatePlaylist();
    }

    private void updatePlaylist() {
        System.out.println("Refreshing playlist");
        mpdComm.getPlaylist().playlistChanged(null, -1);
        System.out.println("Sending playlist");
        MPDPlaylist playlist = mpdComm.getPlaylist();
        write(new MPDResponse(MPDResponse.PLAYER_UPDATE_PLAYLIST, playlist.getMusicList()));
        System.out.println("Done");
    }

    @Override
    public void trackChanged(MPDStatus mpdStatus, int oldTrack) {
        System.out.println("Track changed");
        write(new MPDResponse(MPDResponse.PLAYER_UPDATE_CURRENTSONG, getCurrentSong(mpdStatus)));
    }

    private Music getCurrentSong(MPDStatus status) {
        int position = status.getSongPos();
        return mpdComm.getPlaylist().getByIndex(position);
    }

    @Override
    public void stateChanged(MPDStatus mpdStatus, String oldState) {
        System.out.println("State changed");

    }

    @Override
    public void repeatChanged(boolean repeating) {
        System.out.println("Repeat changed");
    }

    @Override
    public void randomChanged(boolean random) {
        System.out.println("Random changed");
    }

    @Override
    public void connectionStateChanged(boolean connected, boolean connectionLost) {
        System.out.println("Connection State changed");
    }

    @Override
    public void libraryStateChanged(boolean updating) {
        System.out.println("Library state changed");
    }

    @Override
    public void trackPositionChanged(MPDStatus status) {
        write(new MPDResponse(MPDResponse.PLAYER_UPDATE_TRACK_POSITION, status.getElapsedTime()));
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

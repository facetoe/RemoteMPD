package org.a0z.mpd;

import org.a0z.mpd.exception.MPDException;
import org.a0z.mpd.exception.MPDServerException;

import java.util.ArrayList;
import java.util.List;

/**
 * dmixUnmodified
 * Created by facetoe on 20/02/14.
 */
abstract public class AbstractMPDConnection {

    protected static final String MPD_RESPONSE_ERR = "ACK";
    protected static final String MPD_RESPONSE_OK = "OK";
    protected static final String MPD_CMD_START_BULK = "command_list_begin";
    protected static final String MPD_CMD_START_BULK_OK = "command_list_ok_begin";
    protected static final String MPD_CMD_BULK_SEP = "list_OK";
    protected static final String MPD_CMD_END_BULK = "command_list_end";

    protected int[] mpdVersion;
    protected List<AbstractCommand> commandQueue;

    abstract int[] connect() throws MPDServerException;

    abstract void disconnect() throws MPDServerException;

    abstract public boolean isConnected();

    abstract int[] getMpdVersion();

    abstract public List<String> sendCommand(AbstractCommand command) throws MPDServerException;

    abstract public List<String> sendCommand(String command, String... args) throws MPDServerException;

    abstract public void queueCommand(String command, String... args);

    abstract public void queueCommand(AbstractCommand command);

    abstract public List<String[]> sendCommandQueueSeparated() throws MPDServerException;

    abstract public List<String> sendCommandQueue() throws MPDServerException;

    abstract List<String> sendCommandQueue(boolean withSeparator) throws MPDServerException;

    abstract public List<String> sendRawCommand(AbstractCommand command) throws MPDServerException;

    static List<String[]> separatedQueueResults(List<String> lines) {
        List<String[]> result = new ArrayList<String[]>();
        ArrayList<String> lineCache = new ArrayList<String>();

        for (String line : lines) {
            if (line.equals(MPD_CMD_BULK_SEP)) { // new part
                if (lineCache.size() != 0) {
                    result.add((String[]) lineCache.toArray(new String[0]));
                    lineCache.clear();
                }
            } else
                lineCache.add(line);
        }
        if (lineCache.size() != 0) {
            result.add((String[]) lineCache.toArray(new String[0]));
        }
        return result;
    }
}

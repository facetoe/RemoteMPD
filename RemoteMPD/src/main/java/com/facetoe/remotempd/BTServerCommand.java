package com.facetoe.remotempd;

import org.a0z.mpd.AbstractCommand;

/**
 * RemoteMPD
 * Created by facetoe on 8/02/14.
 */
public class BTServerCommand extends AbstractCommand {
    public static final String REQUEST_PLAYLIST_HASH =  "request_playlist_hash";

    public BTServerCommand(String _command, String... _args) {
        super(_command, _args);
    }
}

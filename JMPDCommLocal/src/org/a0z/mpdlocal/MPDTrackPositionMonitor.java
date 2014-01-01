package org.a0z.mpdlocal;

import org.a0z.mpdlocal.event.TrackPositionListener;
import org.a0z.mpdlocal.exception.MPDServerException;

/**
 * Created by facetoe on 30/12/13.
 */
public class MPDTrackPositionMonitor extends MPDMonitor {

    public MPDTrackPositionMonitor(MPD mpd, int delay) {
        super(mpd, delay);
        setName("MPDTrackPositionMonitor");
    }

    @Override
    public void run() {
        long oldElapsedTime = -1;

        while (!giveup) {
            Boolean connectionState = Boolean.valueOf(mpd.isConnected());
            if (connectionState == Boolean.TRUE) {
                try {
                    MPDStatus status = mpd.getStatus(true);
                    if (oldElapsedTime != status.getElapsedTime()) {
                        for (TrackPositionListener listener : trackPositionChangedListeners)
                            listener.trackPositionChanged(status);
                        oldElapsedTime = status.getElapsedTime();
                    }
                } catch (MPDServerException e) {
                    e.printStackTrace();
                }
            }
            try {
                synchronized (this) {
                    this.wait(this.delay);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

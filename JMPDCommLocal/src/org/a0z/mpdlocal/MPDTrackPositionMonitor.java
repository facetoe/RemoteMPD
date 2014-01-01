package org.a0z.mpdlocal;

import org.a0z.mpdlocal.event.TrackPositionListener;
import org.a0z.mpdlocal.exception.MPDServerException;

/**
 * Created by facetoe on 30/12/13.
 */
public class MPDTrackPositionMonitor extends MPDMonitor {

    private boolean paused = false;

    public MPDTrackPositionMonitor(MPD mpd, int delay) {
        super(mpd, delay);
        setName("MPDTrackPositionMonitor");
    }

    @Override
    public void run() {
        long oldElapsedTime = -1;

        while (!giveup) {
            Boolean mpdConnectionState = Boolean.valueOf(mpd.isConnected());
            if (mpdConnectionState == Boolean.TRUE && !paused) {
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

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}

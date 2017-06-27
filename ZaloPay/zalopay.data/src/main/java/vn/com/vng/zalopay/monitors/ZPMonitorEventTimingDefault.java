package vn.com.vng.zalopay.monitors;

import timber.log.Timber;

/**
 * Created by huuhoa on 6/27/17.
 * Default implementation for monitor event timing
 */

public class ZPMonitorEventTimingDefault implements ZPMonitorEventTiming {
    /**
     * Record an event that happens at the time of calling this method
     *
     * @param event eventId
     */
    @Override
    public void recordEvent(ZPMonitorEvent event) {
        Timber.d("Event %s at %s", event, System.currentTimeMillis());
    }
}

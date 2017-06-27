package vn.com.vng.zalopay.monitors;

/**
 * Created by huuhoa on 6/27/17.
 * Monitor timing for events
 */

public interface ZPMonitorEventTiming {
    /**
     * Record an event that happens at the time of calling this method
     * @param event eventId
     */
    void recordEvent(ZPMonitorEvent event);
}

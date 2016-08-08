package vn.com.vng.zalopay.analytics;

/**
 * Created by huuhoa on 6/27/16.
 * Declaration of trackers
 */
public interface ZPTracker {
    /**
     * Log custom event. Events are defined in https://docs.google.com/spreadsheets/d/1kdqC78-qMsRGY_n4hMyzUsbHlS3Gl4yWwgr62qLO8Co/edit#gid=0
     * @param eventId Id of the event that we want to log
     * @param eventValue (optional) provide value for a given event
     */
    void trackEvent(int eventId, Long eventValue);

    void trackScreen(String screenName);

    /**
     * Log timing value for given event.
     * Events are defined in https://docs.google.com/spreadsheets/d/1kdqC78-qMsRGY_n4hMyzUsbHlS3Gl4yWwgr62qLO8Co/edit#gid=0
     * @param eventId If of timing event
     * @param value time recorded in milliseconds
     */
    void trackTiming(int eventId, long value);
}

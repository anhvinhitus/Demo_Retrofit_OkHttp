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
    void logEvent(int eventId, Long eventValue);

    void logScreenView(String screenName);
}

package vn.com.zalopay.analytics;

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

    /**
     * Log apptransid.
     * @param apptransid If of order created by merchant apps
     * @param appid (optional) id of merchant app
     * @param step (optional) id of order payment step
     * @param step_result (optional) result value of payment step
     * @param pcmid (optional) payment channel
     * @param transtype (optional) transtype of order
     * @param transid (optional) transactionid from server corresponding with apptransid
     * @param sdk_result (optional) result value from sdk
     * @param server_result (optional) order payment result value from server
     * @param source (optional) source of apptransid
     */
    void trackApptransidEvent(String apptransid, int appid, int step, int step_result, int pcmid, int transtype,
                    long transid, int sdk_result, int server_result, String source);
}

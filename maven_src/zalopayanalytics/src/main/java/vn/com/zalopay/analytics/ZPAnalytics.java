package vn.com.zalopay.analytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class ZPAnalytics {
    static List<ZPTracker> sTrackerList = new ArrayList<>();

    /**
     * Log custom event. Events are defined in https://docs.google.com/spreadsheets/d/1kdqC78-qMsRGY_n4hMyzUsbHlS3Gl4yWwgr62qLO8Co/edit#gid=0
     *
     * @param eventId Id of the event that we want to log
     */
    public static void trackEvent(int eventId) {
        trackEvent(eventId, null);
    }

    /**
     * Log custom event. Events are defined in https://docs.google.com/spreadsheets/d/1kdqC78-qMsRGY_n4hMyzUsbHlS3Gl4yWwgr62qLO8Co/edit#gid=0
     *
     * @param eventId    Id of the event that we want to log
     * @param eventValue (optional) provide value for a given event
     */
    public static void trackEvent(int eventId, Long eventValue) {
        for (ZPTracker tracker : sTrackerList) {
            tracker.trackEvent(eventId, eventValue);
        }
    }

    public static void trackScreen(String screenName) {
        for (ZPTracker tracker : sTrackerList) {
            tracker.trackScreen(screenName);
        }
    }

    public static void trackTiming(int eventId, long value) {
        for (ZPTracker tracker : sTrackerList) {
            tracker.trackTiming(eventId, value);
        }
    }

    public static void trackApptransidEvent(ZPApptransidLog log) {
        for (ZPTracker tracker : sTrackerList) {
            tracker.trackApptransidEvent(log);
        }
    }

    public static void trackApptransidApiCall(ZPApptransidLogApiCall log) {
        for (ZPTracker tracker : sTrackerList) {
            tracker.trackApptransidApiCall(log);
        }
    }

    public static void trackAPIError(String apiName, int httpCode, int serverCode, int networkCode) {
        for (ZPTracker tracker : sTrackerList) {
            tracker.trackAPIError(apiName, httpCode, serverCode, networkCode);
        }
    }

    public static void trackConnectorError(String currentUid, String receivedUid, long mtuid, int sourceid, long timestamp) {
        for (ZPTracker tracker : sTrackerList) {
            tracker.trackConnectorError(currentUid, receivedUid, mtuid, sourceid, timestamp);
        }
    }

    public static void addTracker(ZPTracker tracker) {
        if (sTrackerList.contains(tracker)) {
            return;
        }

        sTrackerList.add(tracker);
    }

    public static void addDefaultTracker() {
        addTracker(new DefaultTracker());
    }

    public static void removeTracker(ZPTracker tracker) {
        if (sTrackerList.contains(tracker)) {
            sTrackerList.remove(tracker);
        }
    }

    public static void removeAll() {
        sTrackerList.clear();
    }

    static class DefaultTracker implements ZPTracker {

        @Override
        public void trackEvent(int eventId, Long eventValue) {
            String message;
            if (eventValue == null) {
                message = String.format(Locale.getDefault(), "Event [%d]", eventId);
            } else {
                message = String.format(Locale.getDefault(), "Event [%d] - value [%s]", eventId, eventValue);
            }

            Timber.tag("ZPAnalytics").d(message);
        }

        @Override
        public void trackScreen(String screenName) {
            Timber.tag("ZPAnalytics").d("Screen [%s]", screenName);
        }

        @Override
        public void trackTiming(int eventId, long value) {
            Timber.tag("ZPAnalytics").d("Timing [%s-%s]", eventId, value);
        }

        @Override
        public void trackApptransidEvent(ZPApptransidLog log) {
            Timber.tag("ZPAnalytics").d("Apptransid Log [%s-%s-%s-%s-%s-%s-%s-%s-%s-%s-%s-%s-%s-%s]",
                    log.apptransid, log.appid, log.step, log.step_result, log.pcmid, log.transtype, log.transid,
                    log.sdk_result, log.server_result, log.source, log.start_time, log.finish_time, log.bank_code, log.status);
        }

        @Override
        public void trackApptransidApiCall(ZPApptransidLogApiCall log) {
            Timber.tag("ZPAnalytics").d("Apptransid Log Api Call [%s-%s-%s-%s-%s]",
                    log.apptransid, log.apiid, log.time_begin, log.time_end, log.return_code);
        }

        @Override
        public void trackAPIError(String apiName, int httpCode, int serverCode, int networkCode) {
            Timber.tag("ZPAnalytics").d("API Error [%s-%s-%s-%s]", apiName, httpCode, serverCode, networkCode);
        }

        @Override
        public void trackConnectorError(String currentUid, String receivedUid, long mtuid, int sourceid, long timestamp) {
            Timber.tag("ZPAnalytics").e("Connector Error [%s-%s-%s-%s-%s]", currentUid, receivedUid, mtuid, sourceid, timestamp);
        }
    }
}

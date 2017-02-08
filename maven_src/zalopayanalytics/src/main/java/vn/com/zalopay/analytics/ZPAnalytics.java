package vn.com.zalopay.analytics;

import java.util.ArrayList;
import java.util.List;

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

    public static void trackApptransidEvent(String apptransid, int appid, int step, int step_result,
                                            int pcmid, int transtype, long transid, int sdk_result,
                                            int server_result, String source) {
        for (ZPTracker tracker : sTrackerList) {
            tracker.trackApptransidEvent(apptransid, appid, step, step_result,
                    pcmid, transtype, transid, sdk_result, server_result, source);
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
                message = String.format("Event [%d]", eventId);
            } else {
                message = String.format("Event [%d] - value [%s]", eventId, eventValue);
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
        public void trackApptransidEvent(String apptransid, int appid, int step, int step_result,
                                         int pcmid, int transtype, long transid, int sdk_result,
                                         int server_result, String source) {
            Timber.tag("ZPAnalytics").d("Apptransid Event [%s-%s-%s-%s-%s-%s-%s-%s-%s-%s]",
                    apptransid, appid, step, step_result, pcmid, transtype, transid,
                    sdk_result, server_result, source);
        }
    }
}

package vn.com.vng.zalopay.analytics;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class ZPAnalytics {
    private final List<ZPTracker> mTrackerList;

    ZPAnalytics(List<ZPTracker> trackerList) {
        this.mTrackerList = trackerList;
    }

    /**
     * Log custom event. Events are defined in https://docs.google.com/spreadsheets/d/1kdqC78-qMsRGY_n4hMyzUsbHlS3Gl4yWwgr62qLO8Co/edit#gid=0
     *
     * @param eventId Id of the event that we want to log
     */
    public void trackEvent(int eventId) {
        trackEvent(eventId, null);
    }

    /**
     * Log custom event. Events are defined in https://docs.google.com/spreadsheets/d/1kdqC78-qMsRGY_n4hMyzUsbHlS3Gl4yWwgr62qLO8Co/edit#gid=0
     *
     * @param eventId    Id of the event that we want to log
     * @param eventValue (optional) provide value for a given event
     */
    public void trackEvent(int eventId, Long eventValue) {
        for (ZPTracker tracker : mTrackerList) {
            tracker.trackEvent(eventId, eventValue);
        }
    }

    public void trackScreen(String screenName) {
        for (ZPTracker tracker : mTrackerList) {
            tracker.trackScreen(screenName);
        }
    }

    public static class Builder {
        private final List<ZPTracker> mTrackerList;

        public Builder() {
            mTrackerList = new ArrayList<>();
        }

        public Builder addTracker(ZPTracker tracker) {
            if (mTrackerList.contains(tracker)) {
                return this;
            }

            mTrackerList.add(tracker);
            return this;
        }

        public Builder addDefaultTracker() {
            return addTracker(new DefaultTracker());
        }

        public ZPAnalytics build() {
            return new ZPAnalytics(mTrackerList);
        }
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

            Timber.tag("ZPAnalytics").i(message);
        }

        @Override
        public void trackScreen(String screenName) {
            //Timber.tag("ZPAnalytics").d(screenName);
        }
    }
}

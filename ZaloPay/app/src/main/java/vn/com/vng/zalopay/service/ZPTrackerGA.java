package vn.com.vng.zalopay.service;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import vn.com.vng.zalopay.analytics.ZPAnalytics;
import vn.com.vng.zalopay.analytics.ZPTracker;

/**
 * Created by huuhoa on 6/27/16.
 * Default implementation for translate ZPTracker to Google Analytics
 */
public class ZPTrackerGA implements ZPTracker {
    private final Tracker mTracker;

    public ZPTrackerGA(Tracker tracker) {

        mTracker = tracker;
    }
    @Override
    public void logEvent(int eventId, Long eventValue) {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(getEventCategory(eventId))
                .setAction(getEventAction(eventId))
                .setLabel("")
                .setValue(eventValue)
                .build());
    }

    private String getEventAction(int eventId) {
        switch (eventId) {
            case ZPAnalytics.APP_LAUNCH:
                return "App Launch";
            default:
                return String.valueOf(eventId);
        }
    }

    private String getEventCategory(int eventId) {
        if (eventId < ZPAnalytics.END_STARTUP) {
            return "Startup";
        } else if (eventId < ZPAnalytics.END_LOGIN) {
            return "Login";
        } else {
            return "Unknown Category";
        }
    }
}

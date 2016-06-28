package vn.com.vng.zalopay.service;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import vn.com.vng.zalopay.analytics.ZPAnalytics;
import vn.com.vng.zalopay.analytics.ZPEvents;
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
                .setCategory(ZPEvents.categoryFromEventId(eventId))
                .setAction(ZPEvents.actionFromEventId(eventId))
                .setLabel("")
                .setValue(eventValue)
                .build());
    }
}

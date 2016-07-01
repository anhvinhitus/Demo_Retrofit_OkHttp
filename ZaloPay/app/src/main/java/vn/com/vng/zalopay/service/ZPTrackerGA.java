package vn.com.vng.zalopay.service;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import vn.com.vng.zalopay.analytics.ZPEvents;
import vn.com.vng.zalopay.analytics.ZPTracker;

/**
 * Created by huuhoa on 6/27/16.
 * Default implementation for translate ZPTracker to Google Analytics
 */
public class ZPTrackerGA implements ZPTracker {

    private static final String FORMAT_GOOGLE_ANALYTICS = "[Android][%s][%s]";

    private final Tracker mTracker;
    private String versionName;

    public ZPTrackerGA(Tracker tracker, Context context) {
        mTracker = tracker;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = pInfo.versionName;
        } catch (Exception ex) {
        }
    }

    @Override
    public void logEvent(int eventId, Long eventValue) {

        HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                .setCategory(ZPEvents.categoryFromEventId(eventId))
                .setAction(ZPEvents.actionFromEventId(eventId))
                .setLabel("");

        if (eventValue != null) {
            eventBuilder.setValue(eventValue);
        }

        mTracker.send(eventBuilder.build());
    }

    @Override
    public void logScreenView(String screenName) {
        String screenWithFormat = String.format(FORMAT_GOOGLE_ANALYTICS, versionName, screenName);
        mTracker.setScreenName(screenWithFormat);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}

package vn.com.vng.zalopay.tracker;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import vn.com.vng.zalopay.BuildConfig;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by huuhoa on 6/27/16.
 * Default implementation for translate ZPTracker to Google Analytics
 */
public class ZPTrackerGA extends DefaultTracker {

    private static final String FORMAT_GOOGLE_ANALYTICS = "[Android][%s]";

    private final Tracker mTracker;

    public ZPTrackerGA(Context context) {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
        mTracker = analytics.newTracker(BuildConfig.GA_Tracker);
    }

    @Override
    public void trackEvent(int eventId, Long eventValue) {

        HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                .setCategory(ZPEvents.categoryFromEventId(eventId))
                .setAction(ZPEvents.actionFromEventId(eventId))
                .setLabel(ZPEvents.actionFromEventId(eventId));

//        if (eventValue != null) {
//            eventBuilder.setValue(eventValue);
//        }

        mTracker.send(eventBuilder.build());
    }

    @Override
    public void trackScreen(String screenName) {
        String screenWithFormat = String.format(FORMAT_GOOGLE_ANALYTICS, screenName);
        mTracker.setScreenName(screenWithFormat);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    /**
     * Log timing value for given event.
     * Events are defined in https://docs.google.com/spreadsheets/d/1kdqC78-qMsRGY_n4hMyzUsbHlS3Gl4yWwgr62qLO8Co/edit#gid=0
     *
     * @param eventId If of timing event
     * @param value   time recorded in milliseconds
     */
    @Override
    public void trackTiming(int eventId, long value) {
        mTracker.send(new HitBuilders.TimingBuilder()
                .setCategory(ZPEvents.categoryFromEventId(eventId))
                .setValue(value)
                .setLabel(ZPEvents.actionFromEventId(eventId))
                .setVariable(ZPEvents.actionFromEventId(eventId))
                .build());
    }
}

package vn.com.vng.zalopay.service;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.analytics.HitBuilders;

import vn.com.vng.zalopay.analytics.ZPEvents;
import vn.com.vng.zalopay.analytics.ZPTracker;

/**
 * Created by huuhoa on 7/17/16.
 * Default implementation for translate ZPTracker to Fabric Answers
 */
public class ZPTrackerAnswers implements ZPTracker {
    @Override
    public void trackEvent(int eventId, Long eventValue) {
        CustomEvent event = new CustomEvent(ZPEvents.categoryFromEventId(eventId))
                .putCustomAttribute("action", ZPEvents.actionFromEventId(eventId));
        if (eventValue != null) {
            event.putCustomAttribute("value", eventValue);
        }
        Answers.getInstance().logCustom(event);
    }

    @Override
    public void trackScreen(String screenName) {
        Answers.getInstance().logContentView(new ContentViewEvent().putContentName(screenName).putContentType("Launch"));
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
        CustomEvent event = new CustomEvent(ZPEvents.categoryFromEventId(eventId))
                .putCustomAttribute("value", value);
        Answers.getInstance().logCustom(event);
    }
}

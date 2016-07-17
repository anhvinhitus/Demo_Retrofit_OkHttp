package vn.com.vng.zalopay.service;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;

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
}

package vn.com.vng.zalopay.tracker;

import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.domain.executor.ThreadExecutor;

/**
 * Created by huuhoa on 6/27/16.
 * Default implementation for translate ZPTracker to Google Analytics
 */
public class ZPTrackerGA extends DefaultTracker {

    private static final String FORMAT_GOOGLE_ANALYTICS = "[Android][%s]";

    private final GoogleReporter mGoogleReporter;
    private final ThreadExecutor mThreadExecutor;

    public ZPTrackerGA(GoogleReporter googleReporter) {
        mGoogleReporter = googleReporter;
        mThreadExecutor = AndroidApplication.instance().getAppComponent().threadExecutor();
    }

    @Override
    public void trackEvent(int eventId, Long eventValue) {
        mThreadExecutor.execute(() -> mGoogleReporter.trackEvent(eventId, eventValue));
    }

    @Override
    public void trackScreen(String screenName) {
        mThreadExecutor.execute(() -> {
            String screenWithFormat = String.format(FORMAT_GOOGLE_ANALYTICS, screenName);
            mGoogleReporter.trackScreen(screenWithFormat);
        });
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
        mThreadExecutor.execute(() -> mGoogleReporter.trackTiming(eventId, value));
    }
}

package vn.com.vng.zalopay.tracker;

import android.content.Context;

/**
 * Created by huuhoa on 6/27/16.
 * Default implementation for translate ZPTracker to Google Analytics
 */
public class ZPTrackerGA extends DefaultTracker {

    private static final String FORMAT_GOOGLE_ANALYTICS = "[Android][%s]";


    public ZPTrackerGA(Context context) {

    }

    @Override
    public void trackEvent(int eventId, Long eventValue) {

    }

    @Override
    public void trackScreen(String screenName) {

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
      
    }
}

package vn.com.vng.zalopay.tracker;

import vn.com.zalopay.analytics.ZPApptransidLog;
import vn.com.zalopay.analytics.ZPApptransidLogApiCall;
import vn.com.zalopay.analytics.ZPTracker;

/**
 * Created by hieuvm on 5/24/17.
 * *
 */

abstract class DefaultTracker implements ZPTracker {
    @Override
    public void trackEvent(int i, Long aLong) {

    }

    @Override
    public void trackScreen(String s) {

    }

    @Override
    public void trackTiming(int i, long l) {

    }

    @Override
    public void trackApptransidEvent(ZPApptransidLog zpApptransidLog) {

    }

    @Override
    public void trackApptransidApiCall(ZPApptransidLogApiCall log) {

    }

    @Override
    public void trackAPIError(String s, int i, int i1, int i2) {

    }

    @Override
    public void trackConnectorError(String currentUid, String receivedUid, long mtuid, int sourceid, long timestamp) {

    }
}

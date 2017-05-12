package vn.com.vng.zalopay.tracker;

import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

import vn.com.vng.zalopay.tracker.model.APIFailedLogData;
import vn.com.vng.zalopay.tracker.model.EventLogData;
import vn.com.vng.zalopay.tracker.model.TrackerType;
import vn.com.zalopay.analytics.ZPApptransidLog;
import vn.com.zalopay.analytics.ZPTracker;

/**
 * Created by hieuvm on 4/20/17.
 * ZPTrackerFileLog for translate ZPTracker to ZaloPay Server
 */

public class ZPTrackerFileAppender implements ZPTracker {

    private static volatile Executor executor;

    private static final Object LOCK = new Object();

    private static Executor getExecutor() {
        synchronized (LOCK) {
            if (executor == null) {
                executor = new SerialExecutor();
            }
        }
        return executor;
    }

    @Override
    public void trackEvent(int eventId, Long eventValue) {
        getExecutor().execute(new WriteLogRunnable(new EventLogData(TrackerType.USER_EVENT_TYPE, eventId, eventValue)));
    }

    @Override
    public void trackScreen(String screenName) {

    }

    @Override
    public void trackTiming(int eventId, long value) {
        getExecutor().execute(new WriteLogRunnable(new EventLogData(TrackerType.TIMING_TYPE, eventId, value)));
    }

    @Override
    public void trackApptransidEvent(ZPApptransidLog log) {

    }

    @Override
    public void trackAPIError(String apiName, int httpCode, int serverCode, int networkCode) {
        getExecutor().execute(new WriteAPIFailedRunnable(new APIFailedLogData(apiName, httpCode, serverCode, networkCode)));
    }

    private static class WriteLogRunnable implements Runnable {
        final EventLogData mLogData;

        WriteLogRunnable(@NonNull EventLogData logData) {
            mLogData = logData;
        }

        @Override
        public void run() {
            EventFileLog.Instance.append(mLogData);
        }
    }

    private static class WriteAPIFailedRunnable implements Runnable {

        final APIFailedLogData mLogData;

        WriteAPIFailedRunnable(APIFailedLogData logData) {
            mLogData = logData;
        }

        @Override
        public void run() {
            APIFailedFileLog.Instance.append(mLogData);
        }
    }
}

package vn.com.vng.zalopay.tracker;

import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

import vn.com.vng.zalopay.tracker.model.APIFailedLogData;
import vn.com.vng.zalopay.tracker.model.EventLogData;
import vn.com.vng.zalopay.tracker.model.PaymentConnectorErrorLogData;
import vn.com.vng.zalopay.tracker.model.TrackerType;
import vn.com.zalopay.analytics.ZPApptransidLog;
import vn.com.zalopay.analytics.ZPTracker;

/**
 * Created by hieuvm on 4/20/17.
 * ZPTrackerFileLog for translate ZPTracker to ZaloPay Server
 */

public class ZPTrackerFileAppender extends DefaultTracker {

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
    public void trackTiming(int eventId, long value) {
        getExecutor().execute(new WriteLogRunnable(new EventLogData(TrackerType.TIMING_TYPE, eventId, value)));
    }

    @Override
    public void trackAPIError(String apiName, int httpCode, int serverCode, int networkCode) {
        getExecutor().execute(new WriteAPIFailedRunnable(new APIFailedLogData(apiName, httpCode, serverCode, networkCode)));
    }

    @Override
    public void trackConnectorError(String currentUid, String receivedUid, long mtuid, int sourceid, long timestamp) {
        getExecutor().execute(new PaymentConnectorErrorRunnable(currentUid, receivedUid, mtuid, sourceid, timestamp));
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

    private static class PaymentConnectorErrorRunnable implements Runnable {

        final PaymentConnectorErrorLogData mLogData;

        PaymentConnectorErrorRunnable(String currentUid, String receivedUid, long mtuid, int sourceid, long timestamp) {
            mLogData = new PaymentConnectorErrorLogData(currentUid, receivedUid, mtuid, sourceid, timestamp);
        }

        @Override
        public void run() {
            PaymentConnectorErrorFileLog.Instance.append(mLogData);
        }
    }
}

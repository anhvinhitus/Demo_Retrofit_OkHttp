package vn.com.vng.zalopay.tracker;

import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

import vn.com.zalopay.analytics.ZPTracker;

/**
 * Created by hieuvm on 4/20/17.
 * ZPTrackerFileLog for translate ZPTracker to ZaloPay Server
 */

public class ZPTrackerFileAppender implements ZPTracker {

    private static final int USER_EVENT_TYPE = 1;
    private static final int TIMING_TYPE = 2;

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
        getExecutor().execute(new WriteLogRunnable(new FileLog.LogData(USER_EVENT_TYPE, eventId, eventValue, System.currentTimeMillis())));
    }

    @Override
    public void trackScreen(String screenName) {

    }

    @Override
    public void trackTiming(int eventId, long value) {
        getExecutor().execute(new WriteLogRunnable(new FileLog.LogData(TIMING_TYPE, eventId, value, System.currentTimeMillis())));
    }

    @Override
    public void trackApptransidEvent(String apptransid, int appid, int step, int step_result, int pcmid, int transtype, long transid, int sdk_result, int server_result, String source) {

    }

    private static class WriteLogRunnable implements Runnable {
        final FileLog.LogData mLogData;

        WriteLogRunnable(@NonNull FileLog.LogData logData) {
            mLogData = logData;
        }

        @Override
        public void run() {
            FileLog.Instance.append(mLogData);
        }
    }
}

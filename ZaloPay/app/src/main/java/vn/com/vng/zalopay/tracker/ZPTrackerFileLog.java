package vn.com.vng.zalopay.tracker;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.ArrayDeque;
import java.util.concurrent.Executor;

import timber.log.Timber;
import vn.com.zalopay.analytics.ZPTracker;

/**
 * Created by hieuvm on 4/20/17.
 * *
 */

public class ZPTrackerFileLog implements ZPTracker {

    private int id = 0;

    private static volatile Executor executor;

    private static final Object LOCK = new Object();

    public static Executor getExecutor() {
        synchronized (LOCK) {
            if (executor == null) {
                executor = new SerialExecutor();
            }
        }
        return executor;
    }

    @Override
    public void trackEvent(int eventId, Long eventValue) {
        getExecutor().execute(new WriteLogRunnable(new FileLog.LogData(id++, eventId, eventValue, System.currentTimeMillis())));
    }

    @Override
    public void trackScreen(String screenName) {

    }

    @Override
    public void trackTiming(int eventId, long value) {

    }

    @Override
    public void trackApptransidEvent(String apptransid, int appid, int step, int step_result, int pcmid, int transtype, long transid, int sdk_result, int server_result, String source) {

    }

    private static class SerialExecutor implements Executor {
        final ArrayDeque<Runnable> mTasks = new ArrayDeque<>();
        Runnable mActive;

        public synchronized void execute(@NonNull final Runnable r) {
            mTasks.offer(() -> {
                try {
                    r.run();
                } finally {
                    scheduleNext();
                }
            });
            if (mActive == null) {
                scheduleNext();
            }
        }

        private synchronized void scheduleNext() {
            if ((mActive = mTasks.poll()) != null) {
                AsyncTask.THREAD_POOL_EXECUTOR.execute(mActive);
            }
        }
    }

    private static class WriteLogRunnable implements Runnable {
        final FileLog.LogData mLogData;

        WriteLogRunnable(@NonNull FileLog.LogData logData) {
            mLogData = logData;
        }

        @Override
        public void run() {
            FileLog.append(mLogData);
        }
    }
}

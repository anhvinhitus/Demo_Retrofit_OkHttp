package vn.com.vng.zalopay.tracker;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.ArrayDeque;
import java.util.concurrent.Executor;

/**
 * Created by huuhoa on 4/24/17.
 * Serial executor
 */
class SerialExecutor implements Executor {
    private final ArrayDeque<Runnable> mTasks = new ArrayDeque<>();
    private Runnable mActive;

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

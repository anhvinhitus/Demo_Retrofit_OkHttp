package vn.com.vng.zalopay.data.ws.connection;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by huuhoa on 8/15/16.
 * heart beat keeper
 */
class TimerWrapper {
    private final TimerListener mListener;
    private Timer mTimer;
    private TimerTask mTask;

    TimerWrapper(TimerListener listener) {
        mListener = listener;
    }

    void start() {
        stop();

        synchronized (this) {
            mTimer = new Timer();
            mTask = new TimerTask() {
                @Override
                public void run() {
                    mListener.onEvent();
                }
            };

            mTimer.schedule(mTask, mListener.delay(), mListener.period());
        }
    }

    void stop() {
        synchronized (this) {
            if (mTask != null) {
                mTask.cancel();
                mTask = null;
            }

            if (mTimer != null) {
                mTimer.cancel();
                mTimer.purge();
                mTimer = null;
            }
        }
    }
}

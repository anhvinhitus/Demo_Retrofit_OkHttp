package vn.com.vng.iot.debugviewer;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

class Lock {
    private static PowerManager.WakeLock lock;

    private static PowerManager.WakeLock getLock(Context context) {
        if (lock == null) {
            PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

            lock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "vn.com.vng.iot.debugviewer.logcat.lock");
            lock.setReferenceCounted(true);
        }
        return lock;
    }

    public static synchronized void acquire(Context context) {
        WakeLock wakeLock = getLock(context);
        if (!wakeLock.isHeld()) {
            wakeLock.acquire();
        }
    }

    public static synchronized void release() {
        if (lock == null) {
            Log.w(Lock.class.getSimpleName(), "release attempted, but wake lock was null");
        } else {
            if (lock.isHeld()) {
                lock.release();
            } else {
                Log.w(Lock.class.getSimpleName(), "release attempted, but wake lock was not held");
            }
        }
    }
}

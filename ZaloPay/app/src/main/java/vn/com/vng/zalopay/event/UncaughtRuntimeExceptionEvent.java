package vn.com.vng.zalopay.event;

/**
 * Created by huuhoa on 6/23/16.
 * Maybe react-native related exception
 */
public class UncaughtRuntimeExceptionEvent {
    private final Throwable mInnerException;

    public UncaughtRuntimeExceptionEvent(Throwable e) {
        mInnerException = e;
    }

    public Throwable getInnerException() {
        return mInnerException;
    }
}

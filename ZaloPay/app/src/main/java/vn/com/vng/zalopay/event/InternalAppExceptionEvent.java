package vn.com.vng.zalopay.event;

/**
 * Created by huuhoa on 6/23/16.
 * Internal react-native app exception event
 */
public class InternalAppExceptionEvent {
    private final Throwable mInnerException;

    public InternalAppExceptionEvent(Throwable e) {
        mInnerException = e;
    }

    public Throwable getInnerException() {
        return mInnerException;
    }
}

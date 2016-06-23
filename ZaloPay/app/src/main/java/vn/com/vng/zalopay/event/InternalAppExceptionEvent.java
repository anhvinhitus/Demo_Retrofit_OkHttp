package vn.com.vng.zalopay.event;

/**
 * Created by huuhoa on 6/23/16.
 * Internal react-native app exception event
 */
public class InternalAppExceptionEvent {
    private final Exception mInnerException;

    public InternalAppExceptionEvent(Exception e) {
        mInnerException = e;
    }

    public Exception getInnerException() {
        return mInnerException;
    }
}

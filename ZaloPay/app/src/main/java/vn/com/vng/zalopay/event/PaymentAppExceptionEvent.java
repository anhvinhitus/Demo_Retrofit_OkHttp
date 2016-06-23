package vn.com.vng.zalopay.event;

/**
 * Created by huuhoa on 6/23/16.
 * Exception with payment app
 */
public class PaymentAppExceptionEvent {
    private final Throwable mInnerException;
    private final long mAppId;

    public PaymentAppExceptionEvent(Throwable e, long appId) {
        mInnerException = e;
        mAppId = appId;
    }

    public Throwable getInnerException() {
        return mInnerException;
    }

    public long getAppId() {
        return mAppId;
    }
}

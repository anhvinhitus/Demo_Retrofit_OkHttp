package vn.com.vng.zalopay.mdl.internal;

/**
 * Created by huuhoa on 4/29/16.
 * Common exception
 */
class MiniApplicationException extends Exception {
    public MiniApplicationException(String message) {
        super(message);
    }

    public MiniApplicationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}

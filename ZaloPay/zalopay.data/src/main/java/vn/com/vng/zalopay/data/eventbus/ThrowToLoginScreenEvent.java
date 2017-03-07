package vn.com.vng.zalopay.data.eventbus;

import android.support.annotation.NonNull;

import vn.com.vng.zalopay.data.exception.BodyException;

/**
 * Created by hieuvm on 2/20/17.
 */

public class ThrowToLoginScreenEvent {

    private BodyException throwable;

    public ThrowToLoginScreenEvent(@NonNull BodyException throwable) {
        this.throwable = throwable;
    }

    public String getMessage() {
        return throwable.getMessage();
    }

    public BodyException getThrowable() {
        return throwable;
    }
}

package vn.com.vng.zalopay.event;

import android.support.annotation.Nullable;

/**
 * Created by longlv on 10/27/16.
 * Notify to others when user sign out
 */

public class SignOutEvent {
    private final String mMessage;

    public SignOutEvent() {
        mMessage = null;
    }

    public SignOutEvent(String message) {
        mMessage = message;
    }

    @Nullable
    public String getMessage() {
        return mMessage;
    }
}

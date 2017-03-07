package vn.com.vng.zalopay.data.exception;

import android.support.annotation.StringRes;

/**
 * Created by hieuvm on 12/24/16.
 */

public class GenericException extends Throwable {

    public int mErrorCode = -1;
    public int mMessageRes;

    public GenericException(@StringRes int message) {
        this.mMessageRes = message;
    }

    public GenericException(String message) {
        super(message);
    }

    public GenericException(int errorCode, String message) {
        this(message);
        this.mErrorCode = errorCode;
    }
}

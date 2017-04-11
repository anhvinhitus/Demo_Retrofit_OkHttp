package vn.com.vng.zalopay.data.exception;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

/**
 * Created by hieuvm on 4/11/17.
 * Sử dung #ErrorMessageFactory để get message cho ui
 */

public class StringResGenericException extends Throwable {

    @StringRes
    public int mMessageRes;

    public StringResGenericException(@StringRes int message) {
        this.mMessageRes = message;
    }

    @Nullable
    @Override
    public String getMessage() {
        return null;
    }
}

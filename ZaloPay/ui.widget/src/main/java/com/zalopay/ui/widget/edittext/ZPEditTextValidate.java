package com.zalopay.ui.widget.edittext;

import android.support.annotation.NonNull;

/**
 * Created by hieuvm on 11/3/16.
 */

public abstract class ZPEditTextValidate {
    private String mErrorMessage;

    public ZPEditTextValidate(String errorMessage) {
        this.mErrorMessage = errorMessage;
    }

    @NonNull
    public String getErrorMessage() {
        return mErrorMessage;
    }

    public abstract boolean isValid(@NonNull CharSequence s);
}

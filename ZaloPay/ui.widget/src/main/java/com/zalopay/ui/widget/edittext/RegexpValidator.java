package com.zalopay.ui.widget.edittext;

import android.support.annotation.NonNull;

import java.util.regex.Pattern;

/**
 * Created by hieuvm on 11/3/16.
 */

public class RegexpValidator extends ZPEditTextValidate {

    private Pattern pattern;

    public RegexpValidator(@NonNull String errorMessage, @NonNull String regex) {
        super(errorMessage);
        pattern = Pattern.compile(regex);
    }

    public RegexpValidator(@NonNull String errorMessage, @NonNull Pattern pattern) {
        super(errorMessage);
        this.pattern = pattern;
    }

    @Override
    public boolean isValid(@NonNull CharSequence s) {
        return pattern.matcher(s).matches();
    }
}

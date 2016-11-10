package vn.com.vng.zalopay.ui.widget.validate;

import android.support.annotation.NonNull;

import com.zalopay.ui.widget.edittext.ZPEditTextValidate;

/**
 * Created by hieuvm on 11/10/16.
 */

public class MinCharactersValidate extends ZPEditTextValidate {
    private int mMinCharacters;

    public MinCharactersValidate(@NonNull String errorMessage, int minCharacters) {
        super(errorMessage);
        this.mMinCharacters = minCharacters;
    }

    @Override
    public boolean isValid(@NonNull CharSequence s) {
        return s.length() >= mMinCharacters;
    }
}

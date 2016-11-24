package vn.com.vng.zalopay.ui.widget.validate;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.zalopay.ui.widget.edittext.ZPEditTextValidate;

/**
 * Created by hieuvm on 11/25/16.
 */

public class DigitsOnlyValidate extends ZPEditTextValidate {

    public DigitsOnlyValidate(@NonNull String errorMessage) {
        super(errorMessage);
    }

    @Override
    public boolean isValid(@NonNull CharSequence s) {
        return TextUtils.isDigitsOnly(s);
    }
}

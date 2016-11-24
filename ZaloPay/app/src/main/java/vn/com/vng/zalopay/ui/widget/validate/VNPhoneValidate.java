package vn.com.vng.zalopay.ui.widget.validate;

import android.support.annotation.NonNull;

import com.zalopay.ui.widget.edittext.ZPEditTextValidate;

import vn.com.vng.zalopay.utils.ValidateUtil;

/**
 * Created by hieuvm on 11/24/16.
 */

public class VNPhoneValidate extends ZPEditTextValidate {

    public VNPhoneValidate(@NonNull String errorMessage) {
        super(errorMessage);
    }

    @Override
    public boolean isValid(@NonNull CharSequence s) {
        return ValidateUtil.isMobileNumber(s.toString());
    }
}

package vn.com.vng.zalopay.ui.widget.validate;

import android.support.annotation.NonNull;

import com.zalopay.ui.widget.edittext.ZPEditTextValidate;

import java.util.ArrayList;
import java.util.List;

import vn.com.vng.zalopay.data.util.PhoneUtil;
import vn.com.vng.zalopay.domain.model.PhoneFormat;

/**
 * Created by hieuvm on 11/24/16.
 * *
 */

public class VNPhoneValidate {

    private List<ZPEditTextValidate> mValidates;

    public VNPhoneValidate() {
        mValidates = new ArrayList<>();
        PhoneFormat phoneFormat = PhoneUtil.getPhoneFormat();
        if (phoneFormat == null) {
            return;
        }
        mValidates.add(new ZPEditTextValidate(phoneFormat.mInvalidLengthMessage) {
            @Override
            public boolean isValid(@NonNull CharSequence s) {
                return PhoneUtil.validLength(s.toString());
            }
        });
        mValidates.add(new ZPEditTextValidate(phoneFormat.mGeneralMessage) {
            @Override
            public boolean isValid(@NonNull CharSequence s) {
                return PhoneUtil.validPatterns(s.toString());
            }
        });
    }

    public List<ZPEditTextValidate> getValidates() {
        return mValidates;
    }
}

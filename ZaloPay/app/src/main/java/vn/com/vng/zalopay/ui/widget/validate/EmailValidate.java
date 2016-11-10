package vn.com.vng.zalopay.ui.widget.validate;

import android.util.Patterns;

import com.zalopay.ui.widget.edittext.RegexpValidator;

/**
 * Created by hieuvm on 11/10/16.
 */

public class EmailValidate extends RegexpValidator {

    public EmailValidate(String errorMessage) {
        super(errorMessage, Patterns.EMAIL_ADDRESS);
    }
}

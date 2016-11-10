package vn.com.vng.zalopay.ui.widget.validate;

import com.zalopay.ui.widget.edittext.RegexpValidator;

/**
 * Created by hieuvm on 11/10/16.
 */

public class PassportValidate extends RegexpValidator {
    public PassportValidate(String errorMessage) {
        super(errorMessage, "^[a-zA-Z0-9]*");
    }
}

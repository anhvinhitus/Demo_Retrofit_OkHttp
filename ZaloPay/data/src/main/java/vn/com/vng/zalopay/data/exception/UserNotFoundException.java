package vn.com.vng.zalopay.data.exception;

import vn.com.vng.zalopay.data.R;

/**
 * Created by hieuvm on 2/7/17.
 */

public class UserNotFoundException extends GenericException {

    public UserNotFoundException() {
        super(R.string.exception_user_not_existed);
    }
}

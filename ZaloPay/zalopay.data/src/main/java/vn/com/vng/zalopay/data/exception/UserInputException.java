package vn.com.vng.zalopay.data.exception;

import android.support.annotation.StringRes;

/**
 * Created by longlv on 03/27/17.
 * Exception has been thrown when user input invalid.
 *
 * Note: use ErrorMessageFactory.create to getMessage.
 */

public class UserInputException extends GenericException {

    public UserInputException(@StringRes int message) {
        super(message);
    }
}

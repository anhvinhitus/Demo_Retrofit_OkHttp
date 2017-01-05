package vn.com.vng.zalopay.exception;

import vn.com.vng.zalopay.data.exception.GenericException;

/**
 * Created by hieuvm on 1/5/17.
 */

public class FingerprintException extends GenericException {

    public FingerprintException(int errorCode, String message) {
        super(errorCode, message);
    }
}

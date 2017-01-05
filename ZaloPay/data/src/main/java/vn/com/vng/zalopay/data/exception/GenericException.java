package vn.com.vng.zalopay.data.exception;

/**
 * Created by hieuvm on 12/24/16.
 */

public class GenericException extends Throwable {

    public int mErrorCode = -1;

    public GenericException(String message) {
        super(message);
    }

    public GenericException(int errorCode, String message) {
        this(message);
        this.mErrorCode = errorCode;
    }
}

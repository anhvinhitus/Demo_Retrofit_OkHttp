package vn.com.vng.zalopay.exception;

/**
 * Created by hieuvm on 12/1/16.
 */

public class PaymentWrapperException extends Throwable {

    private int mErrorCode = -1; //generic error

    public PaymentWrapperException(int code, String message) {
        super(message);
        this.mErrorCode = code;
    }

    public PaymentWrapperException(String message) {
        this(-1, message);
    }

    public int getErrorCode() {
        return mErrorCode;
    }
}

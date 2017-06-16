package vn.com.zalopay.wallet.exception;

/**
 * Created by chucvv on 6/15/17.
 */

public class InvalidStateException extends RuntimeException {
    public InvalidStateException(String s) {
        super(s);
    }
}

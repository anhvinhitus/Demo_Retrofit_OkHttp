package vn.com.vng.zalopay.data.exception;

/**
 * Created by AnhHieu on 7/14/16.
 */
public class AccountSuspendedException extends BodyException {
    public AccountSuspendedException(int error, String message) {
        super(error, message);
    }
}

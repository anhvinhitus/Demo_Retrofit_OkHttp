package vn.com.vng.zalopay.data.exception;

/**
 * Created by AnhHieu on 3/25/16.
 */
public class BodyException extends Throwable {

    public final int errorCode;
    public String message = "Oops";

    public BodyException(int error) {
        this.errorCode = error;
    }

    public BodyException(int error, String message) {
        this.errorCode = error;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

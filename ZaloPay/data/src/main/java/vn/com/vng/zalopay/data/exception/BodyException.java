package vn.com.vng.zalopay.data.exception;

/**
 * Created by AnhHieu on 3/25/16.
 */
public class BodyException extends Throwable {

    public final int errorCode;

    public BodyException(int error) {
        this.errorCode = error;
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}

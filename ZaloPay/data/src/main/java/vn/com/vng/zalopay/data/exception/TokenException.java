package vn.com.vng.zalopay.data.exception;

/**
 * Created by AnhHieu on 6/14/16.
 */
public class TokenException extends Throwable {
    public String message;

    public TokenException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

package vn.com.vng.zalopay.data.exception;

/**
 * Created by AnhHieu on 6/14/16.
 */
public class TokenException extends BodyException {

    public TokenException(int error) {
        super(error);
    }

    public TokenException(int error, String message) {
        super(error, message);
    }
}

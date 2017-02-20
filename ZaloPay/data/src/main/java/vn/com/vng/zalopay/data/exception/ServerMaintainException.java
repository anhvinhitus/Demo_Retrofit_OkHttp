package vn.com.vng.zalopay.data.exception;

/**
 * Created by huuhoa on 6/22/16.
 * Exception for server maintain
 */
public class ServerMaintainException extends BodyException {
    public ServerMaintainException(int error, String message) {
        super(error, message);
    }
}

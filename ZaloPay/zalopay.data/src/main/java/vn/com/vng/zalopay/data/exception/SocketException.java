package vn.com.vng.zalopay.data.exception;

/**
 * Created by AnhHieu on 7/23/16.
 */
public class SocketException extends Exception {

    public int code;

    public SocketException(int code, String message) {
        super(message);
        this.code = code;
    }

    public SocketException(String detailMessage) {
        super(detailMessage);
    }
}

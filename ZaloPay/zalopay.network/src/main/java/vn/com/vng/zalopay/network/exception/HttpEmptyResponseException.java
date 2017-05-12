package vn.com.vng.zalopay.network.exception;

/**
 * Created by huuhoa on 7/4/16.
 * Exception for empty HTTP response
 */
public class HttpEmptyResponseException extends Throwable {
    @Override
    public String getMessage() {
        return "Empty HTTP response";
    }
}

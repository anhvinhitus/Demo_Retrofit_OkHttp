package vn.com.zalopay.wallet.exception;

/**
 * Created by chucvv on 6/7/17.
 */

public class RequestException extends SdkException {
    public static final int NULL = -2000;
    public int code;

    public RequestException(int pCode, String pMessage) {
        super(pMessage);
    }
}

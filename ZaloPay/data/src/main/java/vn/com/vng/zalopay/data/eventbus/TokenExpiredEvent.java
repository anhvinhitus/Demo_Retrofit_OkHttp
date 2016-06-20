package vn.com.vng.zalopay.data.eventbus;

/**
 * Created by AnhHieu on 6/13/16.
 */
public class TokenExpiredEvent {

    public final int errorCode;

    public TokenExpiredEvent(int errorCode) {
        this.errorCode = errorCode;
    }
}

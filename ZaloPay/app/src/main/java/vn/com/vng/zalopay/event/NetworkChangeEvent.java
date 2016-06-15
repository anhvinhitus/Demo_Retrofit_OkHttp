package vn.com.vng.zalopay.event;

/**
 * Created by AnhHieu on 6/15/16.
 */
public class NetworkChangeEvent {
    public final boolean isOnline;

    public NetworkChangeEvent(boolean isOnline) {
        this.isOnline = isOnline;
    }
}

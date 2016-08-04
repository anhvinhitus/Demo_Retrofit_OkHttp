package vn.com.vng.zalopay.data.eventbus;

/**
 * Created by AnhHieu on 6/22/16.
 */
public class NotificationChangeEvent {
    public final boolean read;

    public NotificationChangeEvent(boolean read) {
        this.read = read;
    }
}

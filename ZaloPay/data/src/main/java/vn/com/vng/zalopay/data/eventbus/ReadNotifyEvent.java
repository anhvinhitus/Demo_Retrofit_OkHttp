package vn.com.vng.zalopay.data.eventbus;

/**
 * Created by AnhHieu on 7/25/16.
 */
public class ReadNotifyEvent {
    public long notificationId;

    public ReadNotifyEvent(long notificationId) {
        this.notificationId = notificationId;
    }

}

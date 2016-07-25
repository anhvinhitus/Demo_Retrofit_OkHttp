package vn.com.vng.zalopay.data.eventbus;

/**
 * Created by AnhHieu on 7/25/16.
 */
public class ReadNotifyEvent {
    public long notificationId;
    public int notificationType;

    public ReadNotifyEvent(long notificationId, int notificationType) {
        this.notificationId = notificationId;
        this.notificationType = notificationType;
    }
}

package vn.com.vng.zalopay.event;

import vn.com.vng.zalopay.data.ws.model.NotificationData;

/**
 * Created by AnhHieu on 10/5/16.
 * *
 */

public class AlertNotificationEvent {
    public NotificationData notify;
    public String mTitle;

    public AlertNotificationEvent(NotificationData notify) {
        this.notify = notify;
    }
}
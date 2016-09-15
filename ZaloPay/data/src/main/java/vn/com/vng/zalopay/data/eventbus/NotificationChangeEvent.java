package vn.com.vng.zalopay.data.eventbus;

import vn.com.vng.zalopay.domain.Enums;

/**
 * Created by AnhHieu on 6/22/16.
 */
public class NotificationChangeEvent {
    public final int notificationstate;

    public NotificationChangeEvent(int read) {
        this.notificationstate = read;
    }

    public boolean isRead() {
        return notificationstate == Enums.NotificationState.READ.getId();
    }
}

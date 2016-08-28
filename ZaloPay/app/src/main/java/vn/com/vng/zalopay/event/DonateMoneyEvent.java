package vn.com.vng.zalopay.event;

import vn.com.vng.zalopay.data.ws.model.NotificationData;

/**
 * Created by AnhHieu on 8/28/16.
 */
public class DonateMoneyEvent {
    public NotificationData notify;

    public DonateMoneyEvent(NotificationData notify) {
        this.notify = notify;
    }
}

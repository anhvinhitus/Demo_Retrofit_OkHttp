package vn.com.vng.zalopay.event;

import vn.com.vng.zalopay.data.ws.model.NotificationData;

/**
 * Created by AnhHieu on 7/23/16.
 */
public class PaymentDataEvent {

    public long appId;
    public String zptranstoken;
    public boolean isAppToApp;
    public boolean isConfirm;

    public NotificationData notification;

    public long timeRequest;

    public PaymentDataEvent(long appId, String zptranstoken, boolean isAppToApp) {
        this(appId, zptranstoken, isAppToApp, false);
    }

    public PaymentDataEvent(long appId, String zptranstoken, boolean isAppToApp, boolean isConfirm) {
        this.appId = appId;
        this.zptranstoken = zptranstoken;
        this.isAppToApp = isAppToApp;
        this.isConfirm = isConfirm;
        this.timeRequest = System.currentTimeMillis();
    }
}

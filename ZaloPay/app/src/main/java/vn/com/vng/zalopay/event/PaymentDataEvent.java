package vn.com.vng.zalopay.event;

/**
 * Created by AnhHieu on 7/23/16.
 */
public class PaymentDataEvent {
    public long appId;
    public String zptranstoken;
    public boolean isAppToApp;

    public PaymentDataEvent(long appId, String zptranstoken, boolean isAppToApp) {
        this.appId = appId;
        this.zptranstoken = zptranstoken;
        this.isAppToApp = isAppToApp;
    }
}

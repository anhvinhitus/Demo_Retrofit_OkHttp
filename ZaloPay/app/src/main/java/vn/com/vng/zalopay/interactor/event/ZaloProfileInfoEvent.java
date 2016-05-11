package vn.com.vng.zalopay.interactor.event;

/**
 * Created by AnhHieu on 5/11/16.
 */
public class ZaloProfileInfoEvent {

    public long userId;

    public String displayName;

    public String avatar;

    public ZaloProfileInfoEvent(long userId, String displayName, String avatar) {
        this.userId = userId;
        this.displayName = displayName;
        this.avatar = avatar;
    }
}

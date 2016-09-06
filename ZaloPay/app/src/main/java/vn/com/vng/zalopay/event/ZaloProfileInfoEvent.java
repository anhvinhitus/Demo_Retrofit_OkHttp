package vn.com.vng.zalopay.event;

/**
 * Created by AnhHieu on 5/11/16.
 *
 */
public class ZaloProfileInfoEvent {

    public long userId;

    public String displayName;

    public String avatar;

    public long birthDay;

    public int userGender;

    public ZaloProfileInfoEvent(long userId, String displayName, String avatar, long birthDay, int userGender) {
        this.userId = userId;
        this.displayName = displayName;
        this.avatar = avatar;
        this.birthDay = birthDay;
        this.userGender = userGender;
    }
}

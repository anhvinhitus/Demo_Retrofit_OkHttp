package vn.com.vng.zalopay.domain.model;

/**
 * Created by longlv on 11/06/2016.
 */

@org.parceler.Parcel
public class ZaloFriend extends AbstractData {

    public long userId;
    public String userName;
    public String displayName;
    public String avatar;
    public int userGender;
    public boolean usingApp;

    public String normalizeDisplayName;

    public ZaloFriend() {
        this.userId = -1;
        this.userName = "";
        this.displayName = "";
        this.avatar = "";
        this.userGender = 1;
        this.usingApp = false;
    }

    public long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatar() {
        return avatar;
    }

    public int getUserGender() {
        return userGender;
    }

    public boolean isUsingApp() {
        return usingApp;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setUserGender(int userGender) {
        this.userGender = userGender;
    }

    public void setUsingApp(boolean usingApp) {
        this.usingApp = usingApp;
    }

    public String getNormalizeDisplayName() {
        return normalizeDisplayName;
    }

    public void setNormalizeDisplayName(String normalizeDisplayName) {
        this.normalizeDisplayName = normalizeDisplayName;
    }
}

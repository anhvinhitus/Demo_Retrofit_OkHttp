package vn.com.vng.zalopay.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by longlv on 11/06/2016.
 */
public class ZaloFriend extends AbstractData {
    class Constants {
        /* Zalo profile START */
        public static final String RESULT = "result";
        public static final String USERID = "userId";
        public static final String USERNAME = "userName";
        public static final String DISPLAYNAME = "displayName";
        public static final String AVATAR = "avatar";
        public static final String USERGENDER= "userGender";
        public static final String BIRTHDAY = "birthday";
        public static final String YAHOOID = "yahooId";
        public static final String ZINGMEID = "zingMeId";
        public static final String FACEBOOKID = "facebookId";
        public static final String TWITTERID = "twitterId";
        public static final String GOOGLEID = "googleId";
        public static final String USINGAPP = "usingApp";
    /* Zalo profile END */
    }

    private long userId;
    private String userName;
    private String displayName;
    private String avatar;
    private int userGender;
    private boolean usingApp;

    public ZaloFriend() {
        this.userId = -1;
        this.userName = "";
        this.displayName = "";
        this.avatar = "";
        this.userGender = 1;
        this.usingApp = false;
    }

    public ZaloFriend(long userId, String userName, String displayName, String avatar, int userGender, boolean usingApp) {
        this.userId = userId;
        this.userName = userName;
        this.displayName = displayName;
        this.avatar = avatar;
        this.userGender = userGender;
        this.usingApp = usingApp;
    }

    public ZaloFriend(JSONObject jsonObject) throws JSONException {
        super();
        if (jsonObject == null) {
            return;
        }
//        Timber.d("Profile_jsonObject: %s", jsonObject.toString());
        userId = jsonObject.getLong(Constants.USERID);
        userName = jsonObject.getString(Constants.USERNAME);
        displayName = jsonObject.getString(Constants.DISPLAYNAME);
        avatar = jsonObject.getString(Constants.AVATAR);
        userGender = jsonObject.getInt(Constants.USERGENDER);
        usingApp = jsonObject.getBoolean(Constants.USINGAPP);
    }

    public static final Parcelable.Creator<ZaloFriend> CREATOR = new Parcelable.Creator<ZaloFriend>() {

        @Override
        public ZaloFriend createFromParcel(Parcel parcelSource) {
            // Must read values in the same order as they were placed in
            long userId = parcelSource.readLong();
            String userName = parcelSource.readString();
            String displayName = parcelSource.readString();
            String avatar = parcelSource.readString();
            int userGender = parcelSource.readInt();
            boolean usingApp = parcelSource.readInt() == 1;
            return new ZaloFriend(userId, userName, displayName, avatar, userGender, usingApp);
        }

        @Override
        public ZaloFriend[] newArray(int size) {
            return new ZaloFriend[size];
        }

    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(userId);
        dest.writeString(userName);
        dest.writeString(displayName);
        dest.writeString(avatar);
        dest.writeInt(userGender);
        dest.writeInt(usingApp ? 1 : 0);
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
}

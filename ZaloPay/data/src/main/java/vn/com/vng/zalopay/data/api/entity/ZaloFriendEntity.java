package vn.com.vng.zalopay.data.api.entity;

import org.json.JSONObject;

import vn.com.vng.zalopay.data.util.Strings;

/**
 * Created by AnhHieu on 10/10/16.
 * *
 */

public class ZaloFriendEntity {
    class Constants {
        public static final String RESULT = "result";
        public static final String USERID = "userId";
        public static final String USERNAME = "userName";
        public static final String DISPLAYNAME = "displayName";
        public static final String AVATAR = "avatar";
        public static final String USERGENDER = "userGender";
        public static final String BIRTHDAY = "birthday";
        public static final String YAHOOID = "yahooId";
        public static final String ZINGMEID = "zingMeId";
        public static final String FACEBOOKID = "facebookId";
        public static final String TWITTERID = "twitterId";
        public static final String GOOGLEID = "googleId";
        public static final String USINGAPP = "usingApp";
    }

    public long userId;
    public String userName;
    public String displayName;
    public String avatar;
    public boolean usingApp;

    public String normalizeDisplayName;

    public ZaloFriendEntity() {
    }

    public ZaloFriendEntity(JSONObject jsonObject) {
        userId = jsonObject.optLong(Constants.USERID);
        userName = jsonObject.optString(Constants.USERNAME);
        displayName = jsonObject.optString(Constants.DISPLAYNAME);
        avatar = jsonObject.optString(Constants.AVATAR);
        usingApp = jsonObject.optBoolean(Constants.USINGAPP);
        normalizeDisplayName = Strings.stripAccents(displayName);
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

    public boolean isUsingApp() {
        return usingApp;
    }

    public String getNormalizeDisplayName() {
        return normalizeDisplayName;
    }
}

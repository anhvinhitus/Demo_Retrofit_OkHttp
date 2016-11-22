package vn.com.vng.zalopay.data.api.entity;

import org.json.JSONObject;

import vn.com.vng.zalopay.data.util.Strings;

/**
 * Created by AnhHieu on 10/10/16.
 * *
 */

public class ZaloFriendEntity {
    public static class Constants {
        private static final String USERID = "userId";
        private static final String USERNAME = "userName";
        private static final String DISPLAYNAME = "displayName";
        private static final String AVATAR = "avatar";
        private static final String USINGAPP = "usingApp";
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
}

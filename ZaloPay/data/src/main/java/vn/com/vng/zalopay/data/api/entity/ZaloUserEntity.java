package vn.com.vng.zalopay.data.api.entity;

import org.json.JSONObject;

import vn.com.vng.zalopay.data.util.Strings;

/**
 * Created by AnhHieu on 10/10/16.
 * *
 */

public class ZaloUserEntity {

    private static final String USERID = "userId";
    private static final String USERNAME = "userName";
    private static final String DISPLAYNAME = "displayName";
    private static final String AVATAR = "avatar";
    private static final String USINGAPP = "usingApp";

    public long userId;
    public String userName;
    public String displayName;
    public String avatar;
    public boolean usingApp;
    public String normalizeDisplayName;

    public ZaloUserEntity(long userId) {
        this.userId = userId;
    }

    public ZaloUserEntity(JSONObject jsonObject) {
        userId = jsonObject.optLong(USERID);
        userName = jsonObject.optString(USERNAME);
        displayName = jsonObject.optString(DISPLAYNAME);
        avatar = jsonObject.optString(AVATAR);
        usingApp = jsonObject.optBoolean(USINGAPP);
        normalizeDisplayName = Strings.stripAccents(displayName);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ZaloUserEntity entity = (ZaloUserEntity) o;

        return userId == entity.userId;

    }
}

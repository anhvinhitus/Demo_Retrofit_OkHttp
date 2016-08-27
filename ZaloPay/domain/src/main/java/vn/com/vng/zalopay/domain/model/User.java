package vn.com.vng.zalopay.domain.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AnhHieu on 3/25/16.
 */
@org.parceler.Parcel
public final class User extends Person {

    public String accesstoken;

    public long expirein;

    public String email;

    public String identityNumber;

    public int profilelevel;

    public int need_invitation;

    public List<Permission> profilePermissions;

    public User() {
    }

    public String getSession() {
        return accesstoken;
    }

    public User(String uid) {
        this.zaloPayId = uid;
    }

    public <T> T fromJson(String jsonString, Type type) {
        return new Gson().fromJson(jsonString, type);
    }

    public void setPermissions(String jsonArray) {

        profilePermissions = fromJson(jsonArray,
                new TypeToken<ArrayList<Permission>>() {
                }.getType());
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("zaloPayId: ").append(this.zaloPayId);
        result.append(", ");
        result.append("accesstoken: ").append(this.accesstoken);
        result.append("}");
        return result.toString();
    }
}

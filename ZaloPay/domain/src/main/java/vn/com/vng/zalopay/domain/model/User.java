package vn.com.vng.zalopay.domain.model;

import android.content.SharedPreferences;

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

    public long zaloId;
    public String accesstoken;
    public long expirein;
    public String email;
    public String identityNumber;

    public int profilelevel;

    public int need_invitation;

    public long phonenumber;

    public List<ProfilePermission.Permission> profilePermissions;

    public User() {
    }

    public User(SharedPreferences preferences) {
    }

    public String getSession() {
        return accesstoken;
    }

    public User(String uid) {
        this.uid = uid;
    }

    public <T> T fromJson(String jsonString, Type type) {
        return new Gson().fromJson(jsonString, type);
    }

    public void setPermissions(String jsonArray) {
        profilePermissions = fromJson(jsonArray,
                new TypeToken<ArrayList<ProfilePermission.Permission>>() {
                }.getType());
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("uid: ").append(this.uid);
        result.append(", ");
        result.append("accesstoken: ").append(this.accesstoken);
        result.append("}");
        return result.toString();
    }
}

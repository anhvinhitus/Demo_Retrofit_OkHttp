package vn.com.vng.zalopay.domain.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AnhHieu on 3/25/16.
 *
 */
@org.parceler.Parcel
public final class User extends Person {

    public String accesstoken;

    public long expirein;

    public String email;

    public String identityNumber;

    public int profilelevel;

    public int need_invitation;

    public String profilePermissions;

    public User() {
    }

    public String getSession() {
        return accesstoken;
    }

    public User(String uid) {
        this.zaloPayId = uid;
    }

    public void setPermissions(String jsonArray) {
        profilePermissions = jsonArray;
    }

    @Override
    public String toString() {
        return "{" +
                "zaloPayId: " +
                this.zaloPayId +
                ", " +
                "accesstoken: " +
                this.accesstoken +
                "}";
    }
}

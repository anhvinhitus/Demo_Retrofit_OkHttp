package vn.com.vng.zalopay.domain.model;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AnhHieu on 3/25/16.
 */

/*
*
* */
public final class User extends Person {

    public long zaloId;
    public String accesstoken;
    public long expirein;
    public String email;

    public int profilelevel;
    public List<ProfilePermisssion.Permission> profilePermisssions;

    public User() {
    }

    public User(Parcel source) {
        super(source);
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
        profilePermisssions = fromJson(jsonArray,
                new TypeToken<ArrayList<ProfilePermisssion.Permission>>() {
                }.getType());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }


    public static final Parcelable.Creator<Person> CREATOR = new Parcelable.Creator<Person>() {
        @Override
        public Person createFromParcel(Parcel source) {
            return new Person(source);
        }

        @Override
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };

}

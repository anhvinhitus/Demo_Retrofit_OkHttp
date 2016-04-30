package vn.com.vng.zalopay.domain.model;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by AnhHieu on 3/25/16.
 */

/*
*
* */
public final class User extends Person {

    public String accesstoken;
    public long expirein;

    public String email;

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

    public User(long uid) {
        this.uid = uid;
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

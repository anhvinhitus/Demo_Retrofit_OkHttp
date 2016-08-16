package vn.com.vng.zalopay.domain.model;

import android.text.TextUtils;

import org.parceler.Parcel;

/**
 * Created by AnhHieu on 3/25/16.
 *
 */
@Parcel
public class Person {

    public String uid;

    public String dname;

    public String avatar;

    public long birthDate = 0;

    public int userGender = 1;

    public long phonenumber;

    public Person() {
    }

    public Person(String uid, String dname, String avatar) {
        this.uid = uid;
        this.dname = dname;
        this.avatar = avatar;
    }

    public Person(String uid, String dname, String avatar, long birthDate, int userGender) {
        this.uid = uid;
        this.dname = dname;
        this.avatar = avatar;
        this.birthDate = birthDate;
        this.userGender = userGender;
    }

    public String getGender() {
        return userGender == 1 ? "Nam" : "Nữ";
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Person) {
            return uid == ((Person) o).uid && !TextUtils.isEmpty(uid);
        }
        return false;
    }
}

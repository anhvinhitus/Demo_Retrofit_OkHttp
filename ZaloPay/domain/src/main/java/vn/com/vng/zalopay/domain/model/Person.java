package vn.com.vng.zalopay.domain.model;

import android.text.TextUtils;

import org.parceler.Parcel;

/**
 * Created by AnhHieu on 3/25/16.
 * Person model
 */
@Parcel
public class Person {

    public String zaloPayId;
    public String zalopayname;
    public long zaloId;

    public String displayName;
    public String avatar;
    public long birthDate = 0;
    public int userGender = 1;
    public long phonenumber;

    public Person() {
    }

    public Person(String zaloPayId, String displayName, String avatar) {
        this.zaloPayId = zaloPayId;
        this.displayName = displayName;
        this.avatar = avatar;
    }

    public Person(String zaloPayId, String displayName, String avatar, long birthDate, int userGender) {
        this.zaloPayId = zaloPayId;
        this.displayName = displayName;
        this.avatar = avatar;
        this.birthDate = birthDate;
        this.userGender = userGender;
    }

    public String getGender() {
        return userGender == 1 ? "Nam" : "Nữ";
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Person && zaloPayId.equals(((Person) o).zaloPayId) && !TextUtils.isEmpty(zaloPayId);
    }

    @Override
    public String toString() {
        return "Person {" +
                "zaloPayId='" + zaloPayId + '\'' +
                ", zalopayname='" + zalopayname + '\'' +
                ", displayName='" + displayName + '\'' +
                ", avatar='" + avatar + '\'' +
                ", phonenumber=" + phonenumber +
                '}';
    }
}

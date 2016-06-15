package vn.com.vng.zalopay.domain.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created by AnhHieu on 3/25/16.
 */
public class Person extends AbstractData {

    public String uid;

    public String dname;

    public String avatar;

    public long birthDate = 0;

    public int userGender = 1;

    public Person() {
    }

    public Person(Parcel source) {
        uid = source.readString();
        dname = source.readString();
        avatar = source.readString();
        birthDate = source.readLong();
        userGender = source.readInt();
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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(dname);
        dest.writeString(avatar);
        dest.writeLong(birthDate);
        dest.writeInt(userGender);
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

    @Override
    public boolean equals(Object o) {
        if (o instanceof Person) {
            return uid == ((Person) o).uid && !TextUtils.isEmpty(uid);
        }
        return false;
    }
}

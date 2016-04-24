package vn.com.vng.zalopay.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by AnhHieu on 3/25/16.
 */
public class Person extends AbstractData {

    public long uid;

    public String dname;

    public String avatar;

    public Person() {
    }

    public Person(Parcel source) {
        uid = source.readLong();
        dname = source.readString();
        avatar = source.readString();
    }

    public Person(long uid, String dname, String avatar) {
        this.uid = uid;
        this.dname = dname;
        this.avatar = avatar;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(uid);
        dest.writeString(dname);
        dest.writeString(avatar);
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
            return uid == ((Person) o).uid && uid > 0;
        }
        return false;
    }
}

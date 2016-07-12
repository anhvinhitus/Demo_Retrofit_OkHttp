package vn.com.vng.zalopay.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by AnhHieu on 3/25/16.
 */

/*
*
* */
public final class MappingZaloAndZaloPay extends AbstractData {

    public long zaloId;
    public String zaloPayId;
    public String phonenumber;

    public MappingZaloAndZaloPay() {
        this.zaloId = -1;
        this.zaloPayId = "";
        this.phonenumber = "";
    }

    public MappingZaloAndZaloPay(long zaloId, String zaloPayId, String phonenumber) {
        this.zaloId = zaloId;
        this.zaloPayId = zaloPayId;
        this.phonenumber = phonenumber;
    }

    public MappingZaloAndZaloPay(Parcel source) {
        zaloId = source.readLong();
        zaloPayId = source.readString();
        phonenumber = source.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(zaloId);
        dest.writeString(zaloPayId);
        dest.writeString(phonenumber);
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

    public long getZaloId() {
        return zaloId;
    }

    public String getZaloPayId() {
        return zaloPayId;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setZaloId(long zaloId) {
        this.zaloId = zaloId;
    }

    public void setZaloPayId(String zaloPayId) {
        this.zaloPayId = zaloPayId;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }
}

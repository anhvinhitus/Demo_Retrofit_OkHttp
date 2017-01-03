package vn.com.vng.zalopay.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by longlv on 11/06/2016.
 */

public class ZaloFriend extends AbstractData implements Parcelable {

    public long userId;
    public String zaloPayId;
    public String userName;
    public String displayName;
    public String avatar;
    public boolean usingApp;
    public String normalizeDisplayName;
    public long phonenumber;
    public String zalopayname;
    public long status;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.userId);
        dest.writeString(this.zaloPayId);
        dest.writeString(this.userName);
        dest.writeString(this.displayName);
        dest.writeString(this.avatar);
        dest.writeByte(this.usingApp ? (byte) 1 : (byte) 0);
        dest.writeString(this.normalizeDisplayName);
        dest.writeLong(this.phonenumber);
        dest.writeString(this.zalopayname);
        dest.writeLong(this.status);
    }

    public ZaloFriend() {
    }

    private ZaloFriend(Parcel in) {
        this.userId = in.readLong();
        this.zaloPayId = in.readString();
        this.userName = in.readString();
        this.displayName = in.readString();
        this.avatar = in.readString();
        this.usingApp = in.readByte() != 0;
        this.normalizeDisplayName = in.readString();
        this.phonenumber = in.readLong();
        this.zalopayname = in.readString();
        this.status = in.readLong();
    }

    public static final Creator<ZaloFriend> CREATOR = new Creator<ZaloFriend>() {
        @Override
        public ZaloFriend createFromParcel(Parcel source) {
            return new ZaloFriend(source);
        }

        @Override
        public ZaloFriend[] newArray(int size) {
            return new ZaloFriend[size];
        }
    };
}

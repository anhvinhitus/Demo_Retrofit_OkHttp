package vn.com.vng.zalopay.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by longlv on 11/06/2016.
 */

public class ZaloFriend extends AbstractData implements Parcelable {

    public long userId;
    public String userName;
    public String displayName;
    public String avatar;
    public int userGender;
    public boolean usingApp;

    public String normalizeDisplayName;

    public ZaloFriend() {
        this.userId = -1;
        this.userName = "";
        this.displayName = "";
        this.avatar = "";
        this.userGender = 1;
        this.usingApp = false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.userId);
        dest.writeString(this.userName);
        dest.writeString(this.displayName);
        dest.writeString(this.avatar);
        dest.writeInt(this.userGender);
        dest.writeByte(this.usingApp ? (byte) 1 : (byte) 0);
        dest.writeString(this.normalizeDisplayName);
    }

    protected ZaloFriend(Parcel in) {
        this.userId = in.readLong();
        this.userName = in.readString();
        this.displayName = in.readString();
        this.avatar = in.readString();
        this.userGender = in.readInt();
        this.usingApp = in.readByte() != 0;
        this.normalizeDisplayName = in.readString();
    }

    public static final Parcelable.Creator<ZaloFriend> CREATOR = new Parcelable.Creator<ZaloFriend>() {
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

package vn.com.vng.zalopay.domain.model.redpacket;

import android.os.Parcel;

import vn.com.vng.zalopay.domain.model.AbstractData;

/**
 * Created by longlv on 15/07/2016.
 */
public class PackageInBundle extends AbstractData {

    public long packageID;
    public long bundleID;
    public String revZaloPayID;
    public long revZaloID;
    public String revFullName;
    public String revAvatarURL;
    public long openTime;
    public long amount;
    public String sendMessage;
    public boolean isLuckiest;

    public PackageInBundle(long packageID, long bundleID, String revZaloPayID, long revZaloID, String revFullName, String revAvatarURL, long openTime, long amount, String sendMessage, boolean isLuckiest) {
        this.packageID = packageID;
        this.bundleID = bundleID;
        this.revZaloPayID = revZaloPayID;
        this.revZaloID = revZaloID;
        this.revFullName = revFullName;
        this.revAvatarURL = revAvatarURL;
        this.openTime = openTime;
        this.amount = amount;
        this.sendMessage = sendMessage;
        this.isLuckiest = isLuckiest;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.packageID);
        dest.writeLong(this.bundleID);
        dest.writeString(this.revZaloPayID);
        dest.writeLong(this.revZaloID);
        dest.writeString(this.revFullName);
        dest.writeString(this.revAvatarURL);
        dest.writeLong(this.openTime);
        dest.writeLong(this.amount);
        dest.writeString(this.sendMessage);
        dest.writeByte(this.isLuckiest ? (byte) 1 : (byte) 0);
    }

    protected PackageInBundle(Parcel in) {
        this.packageID = in.readLong();
        this.bundleID = in.readLong();
        this.revZaloPayID = in.readString();
        this.revZaloID = in.readLong();
        this.revFullName = in.readString();
        this.revAvatarURL = in.readString();
        this.openTime = in.readLong();
        this.amount = in.readLong();
        this.sendMessage = in.readString();
        this.isLuckiest = in.readByte() != 0;
    }

    public static final Creator<PackageInBundle> CREATOR = new Creator<PackageInBundle>() {
        @Override
        public PackageInBundle createFromParcel(Parcel source) {
            return new PackageInBundle(source);
        }

        @Override
        public PackageInBundle[] newArray(int size) {
            return new PackageInBundle[size];
        }
    };
}

package vn.com.vng.zalopay.domain.model.redpacket;

import android.os.Parcel;

import vn.com.vng.zalopay.domain.model.AbstractData;

/**
 * Created by longlv on 15/07/2016.
 */
public class ReceivePackage extends AbstractData {

    public long packageID;
    public long bundleID;
    public String revZaloPayID;
    public String sendZaloPayID;
    public String sendFullName;
    public long amount;
    public long openedTime;

    public ReceivePackage(long packageID, long bundleID, String revZaloPayID, String sendZaloPayID, String sendFullName, long amount, long openedTime) {
        this.packageID = packageID;
        this.bundleID = bundleID;
        this.revZaloPayID = revZaloPayID;
        this.sendZaloPayID = sendZaloPayID;
        this.sendFullName = sendFullName;
        this.amount = amount;
        this.openedTime = openedTime;
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
        dest.writeString(this.sendZaloPayID);
        dest.writeString(this.sendFullName);
        dest.writeLong(this.amount);
        dest.writeLong(this.openedTime);
    }

    protected ReceivePackage(Parcel in) {
        this.packageID = in.readLong();
        this.bundleID = in.readLong();
        this.revZaloPayID = in.readString();
        this.sendZaloPayID = in.readString();
        this.sendFullName = in.readString();
        this.amount = in.readLong();
        this.openedTime = in.readLong();
    }

    public static final Creator<ReceivePackage> CREATOR = new Creator<ReceivePackage>() {
        @Override
        public ReceivePackage createFromParcel(Parcel source) {
            return new ReceivePackage(source);
        }

        @Override
        public ReceivePackage[] newArray(int size) {
            return new ReceivePackage[size];
        }
    };
}
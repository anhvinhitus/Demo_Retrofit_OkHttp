package vn.com.vng.zalopay.domain.model.redpacket;

import android.os.Parcel;

import vn.com.vng.zalopay.domain.model.AbstractData;

/**
 * Created by longlv on 18/07/2016.
 */
public class PackageStatus extends AbstractData {
    public boolean isProcessing;
    public String zpTransID;
    public long reqdate;
    public long amount;
    public long balance;
    public String data;

    public PackageStatus(boolean isProcessing, String zpTransID, long reqdate, long amount, long balance, String data) {
        this.isProcessing = isProcessing;
        this.zpTransID = zpTransID;
        this.reqdate = reqdate;
        this.amount = amount;
        this.balance = balance;
        this.data = data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.isProcessing ? (byte) 1 : (byte) 0);
        dest.writeString(this.zpTransID);
        dest.writeLong(this.reqdate);
        dest.writeLong(this.amount);
        dest.writeLong(this.balance);
        dest.writeString(this.data);
    }

    protected PackageStatus(Parcel in) {
        this.isProcessing = in.readByte() != 0;
        this.zpTransID = in.readString();
        this.reqdate = in.readLong();
        this.amount = in.readLong();
        this.balance = in.readLong();
        this.data = in.readString();
    }

    public static final Creator<PackageStatus> CREATOR = new Creator<PackageStatus>() {
        @Override
        public PackageStatus createFromParcel(Parcel source) {
            return new PackageStatus(source);
        }

        @Override
        public PackageStatus[] newArray(int size) {
            return new PackageStatus[size];
        }
    };
}

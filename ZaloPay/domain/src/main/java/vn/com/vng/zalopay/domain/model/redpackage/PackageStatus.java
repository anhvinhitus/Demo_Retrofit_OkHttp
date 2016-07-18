package vn.com.vng.zalopay.domain.model.redpackage;

import android.os.Parcel;

import vn.com.vng.zalopay.domain.model.AbstractData;

/**
 * Created by longlv on 18/07/2016.
 */
public class PackageStatus extends AbstractData {
    public boolean isProcessing;
    public long amount;
    public String zpTransID;
    public String nextAction;
    public String data;
    public long balance;


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.isProcessing ? (byte) 1 : (byte) 0);
        dest.writeLong(this.amount);
        dest.writeString(this.zpTransID);
        dest.writeString(this.nextAction);
        dest.writeString(this.data);
        dest.writeLong(this.balance);
    }

    public PackageStatus(boolean isProcessing, long amount, String zpTransID, String nextAction, String data, long balance) {
        this.isProcessing = isProcessing;
        this.amount = amount;
        this.zpTransID = zpTransID;
        this.nextAction = nextAction;
        this.data = data;
        this.balance = balance;
    }

    protected PackageStatus(Parcel in) {
        this.isProcessing = in.readByte() != 0;
        this.amount = in.readLong();
        this.zpTransID = in.readString();
        this.nextAction = in.readString();
        this.data = in.readString();
        this.balance = in.readLong();
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

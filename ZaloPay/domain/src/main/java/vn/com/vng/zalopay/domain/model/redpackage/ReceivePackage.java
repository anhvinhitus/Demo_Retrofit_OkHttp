package vn.com.vng.zalopay.domain.model.redpackage;

import android.os.Parcel;
import android.os.Parcelable;

import vn.com.vng.zalopay.domain.model.AbstractData;

/**
 * Created by longlv on 15/07/2016.
 */
public class ReceivePackage extends AbstractData {

    public long packageID;
    public long bundleID;
    public String sendZaloPayID;
    public String sendFullName;
    public long amount;
    public long openedTime;

    public ReceivePackage(long packageID, long bundleID, String sendZaloPayID, String sendFullName, long amount, long openedTime) {
        this.packageID = packageID;
        this.bundleID = bundleID;
        this.sendZaloPayID = sendZaloPayID;
        this.sendFullName = sendFullName;
        this.amount = amount;
        this.openedTime = openedTime;
    }

    public ReceivePackage(Parcel in) {
        packageID = in.readLong();
        bundleID = in.readLong();
        sendZaloPayID = in.readString();
        sendFullName = in.readString();
        amount = in.readLong();
        openedTime = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(packageID);
        dest.writeLong(bundleID);
        dest.writeString(sendZaloPayID);
        dest.writeString(sendFullName);
        dest.writeLong(amount);
        dest.writeLong(openedTime);
    }

    public static final Parcelable.Creator<ReceivePackage> CREATOR = new Parcelable.Creator<ReceivePackage>() {
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
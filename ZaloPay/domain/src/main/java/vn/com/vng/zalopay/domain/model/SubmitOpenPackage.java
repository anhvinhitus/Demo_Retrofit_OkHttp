package vn.com.vng.zalopay.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by longlv on 13/07/2016.
 * Relate with RedPackageResponse which is data of request "sendbundle"
 */
public class SubmitOpenPackage extends AbstractData {
    public long bundleID;
    public long packageID;
    public String zpTransID;

    public SubmitOpenPackage(long bundleID, long packageID, String zpTransID) {
        this.bundleID = bundleID;
        this.packageID = packageID;
        this.zpTransID = zpTransID;
    }

    public SubmitOpenPackage(Parcel in) {
        bundleID = in.readLong();
        packageID = in.readLong();
        zpTransID = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(bundleID);
        dest.writeLong(packageID);
        dest.writeString(zpTransID);
    }

    public final Parcelable.Creator<SubmitOpenPackage> CREATOR = new Parcelable.Creator<SubmitOpenPackage>() {
        @Override
        public SubmitOpenPackage createFromParcel(Parcel source) {
            return new SubmitOpenPackage(source);
        }

        @Override
        public SubmitOpenPackage[] newArray(int size) {
            return new SubmitOpenPackage[size];
        }
    };
}

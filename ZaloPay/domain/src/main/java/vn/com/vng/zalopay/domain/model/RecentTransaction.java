package vn.com.vng.zalopay.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by longlv on 11/06/2016.
 */
public class RecentTransaction implements Parcelable {

    public long zaloId;
    public String zaloPayId;
    public String zaloPayName;
    public String displayName;
    public String avatar;
    public String phoneNumber;
    public long amount;
    public String message;

    // Temporary data, don't need to store
    public String transactionId;

    public RecentTransaction() {
    }

    public RecentTransaction(long zaloId, String zaloPayId, String userName, String displayName, String avatar, String phoneNumber, long amount, String message) {
        this.zaloId = zaloId;
        this.zaloPayId = zaloPayId;
        this.zaloPayName = userName;
        this.displayName = displayName;
        this.avatar = avatar;
        this.phoneNumber = phoneNumber;
        this.amount = amount;
        this.message = message;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.zaloId);
        dest.writeString(this.zaloPayId);
        dest.writeString(this.zaloPayName);
        dest.writeString(this.displayName);
        dest.writeString(this.avatar);
        dest.writeString(this.phoneNumber);
        dest.writeLong(this.amount);
        dest.writeString(this.message);
        dest.writeString(this.transactionId);
    }

    protected RecentTransaction(Parcel in) {
        this.zaloId = in.readLong();
        this.zaloPayId = in.readString();
        this.zaloPayName = in.readString();
        this.displayName = in.readString();
        this.avatar = in.readString();
        this.phoneNumber = in.readString();
        this.amount = in.readLong();
        this.message = in.readString();
        this.transactionId = in.readString();
    }

    public static final Parcelable.Creator<RecentTransaction> CREATOR = new Parcelable.Creator<RecentTransaction>() {
        @Override
        public RecentTransaction createFromParcel(Parcel source) {
            return new RecentTransaction(source);
        }

        @Override
        public RecentTransaction[] newArray(int size) {
            return new RecentTransaction[size];
        }
    };
}

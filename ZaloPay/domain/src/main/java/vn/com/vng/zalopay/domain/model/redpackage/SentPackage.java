package vn.com.vng.zalopay.domain.model.redpackage;

import android.os.Parcel;
import android.os.Parcelable;

import vn.com.vng.zalopay.domain.model.AbstractData;

/**
 * Created by longlv on 15/07/2016.
 */
public class SentPackage extends AbstractData {

    public String revZaloPayID;
    public long revZaloID;
    public String revFullName;
    public String revAvatarURL;
    public long openTime;
    public long amount;
    public String sendMessage;
    public boolean isLuckiest;

    public SentPackage(String revZaloPayID, long revZaloID, String revFullName, String revAvatarURL, long openTime, long amount, String sendMessage, boolean isLuckiest) {
        this.revZaloPayID = revZaloPayID;
        this.revZaloID = revZaloID;
        this.revFullName = revFullName;
        this.revAvatarURL = revAvatarURL;
        this.openTime = openTime;
        this.amount = amount;
        this.sendMessage = sendMessage;
        this.isLuckiest = isLuckiest;
    }

    public SentPackage(Parcel in) {
        revZaloPayID = in.readString();
        revZaloID = in.readLong();
        revFullName = in.readString();
        revAvatarURL = in.readString();
        openTime = in.readLong();
        amount = in.readLong();
        sendMessage = in.readString();
        isLuckiest = in.readInt() == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(revZaloPayID);
        dest.writeLong(revZaloID);
        dest.writeString(revFullName);
        dest.writeString(revAvatarURL);
        dest.writeLong(openTime);
        dest.writeLong(amount);
        dest.writeString(revZaloPayID);
        dest.writeInt(isLuckiest?1:0);
    }


    public static final Parcelable.Creator<SentPackage> CREATOR = new Parcelable.Creator<SentPackage>() {
        @Override
        public SentPackage createFromParcel(Parcel source) {
            return new SentPackage(source);
        }

        @Override
        public SentPackage[] newArray(int size) {
            return new SentPackage[size];
        }
    };
}

package vn.com.vng.zalopay.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by longlv on 13/07/2016.
 */
public class RedPackage extends AbstractData {
    private String zpTransID;

    public RedPackage(String zpTransID) {
        this.zpTransID = zpTransID;
    }

    public RedPackage(Parcel in) {
        zpTransID = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(zpTransID);
    }

    public final Parcelable.Creator<RedPackage> CREATOR = new Parcelable.Creator<RedPackage>() {
        @Override
        public RedPackage createFromParcel(Parcel source) {
            return new RedPackage(source);
        }

        @Override
        public RedPackage[] newArray(int size) {
            return new RedPackage[size];
        }
    };
}

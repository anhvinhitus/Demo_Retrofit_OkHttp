package vn.com.vng.zalopay.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by longlv on 13/07/2016.
 */
public class BundleOrder extends Order {

    public String bundleId;

    public BundleOrder(long appid, String zptranstoken, String apptransid, String appuser, long apptime, String embeddata, String item, long amount, String description, String payoption, String mac) {
        super(appid, zptranstoken, apptransid, appuser, apptime, embeddata, item, amount, description, payoption, mac);
    }

    public BundleOrder(long appid, String zptranstoken, String apptransid, String appuser, long apptime, String embeddata, String item, long amount, String description, String payoption, String mac, String bundleId) {
        super(appid, zptranstoken, apptransid, appuser, apptime, embeddata, item, amount, description, payoption, mac);
        this.bundleId = bundleId;
    }

    public BundleOrder(Parcel in) {
        super(in);
        bundleId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(bundleId);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(super.toString());
        stringBuilder.append(bundleId);
        return stringBuilder.toString();
    }

    public final Parcelable.Creator<BundleOrder> CREATOR = new Parcelable.Creator<BundleOrder>() {
        @Override
        public BundleOrder createFromParcel(Parcel source) {
            return new BundleOrder(source);
        }

        @Override
        public BundleOrder[] newArray(int size) {
            return new BundleOrder[size];
        }
    };
}

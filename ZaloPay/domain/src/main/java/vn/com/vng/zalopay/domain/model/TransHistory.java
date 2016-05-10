package vn.com.vng.zalopay.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by AnhHieu on 5/4/16.
 */
public class TransHistory extends AbstractData {

    public TransHistory(long transid) {
        this.transid = transid;
    }

    public String userid;

    public long transid;

    public long appid;

    public String appuser;

    public String platform;

    public String description;

    public int pmcid;

    public long reqdate;

    public int grossamount;

    public int netamount;

    public int type;

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    public TransHistory(Parcel source) {
    }

    public static final Parcelable.Creator<TransHistory> CREATOR = new Parcelable.Creator<TransHistory>() {
        @Override
        public TransHistory createFromParcel(Parcel source) {
            return new TransHistory(source);
        }

        @Override
        public TransHistory[] newArray(int size) {
            return new TransHistory[size];
        }
    };
}

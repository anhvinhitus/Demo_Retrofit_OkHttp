package vn.com.vng.zalopay.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by AnhHieu on 5/4/16.
 */

public class TransHistory extends AbstractData implements Parcelable {

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

    public int userchargeamt;

    public int userfeeamt;

    public int amount;

    public int type;

    public int sign;

    public String username;

    public String appusername;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userid);
        dest.writeLong(this.transid);
        dest.writeLong(this.appid);
        dest.writeString(this.appuser);
        dest.writeString(this.platform);
        dest.writeString(this.description);
        dest.writeInt(this.pmcid);
        dest.writeLong(this.reqdate);
        dest.writeInt(this.userchargeamt);
        dest.writeInt(this.userfeeamt);
        dest.writeInt(this.amount);
        dest.writeInt(this.type);
        dest.writeInt(this.sign);
        dest.writeString(this.username);
        dest.writeString(this.appusername);
    }

    protected TransHistory(Parcel in) {
        this.userid = in.readString();
        this.transid = in.readLong();
        this.appid = in.readLong();
        this.appuser = in.readString();
        this.platform = in.readString();
        this.description = in.readString();
        this.pmcid = in.readInt();
        this.reqdate = in.readLong();
        this.userchargeamt = in.readInt();
        this.userfeeamt = in.readInt();
        this.amount = in.readInt();
        this.type = in.readInt();
        this.sign = in.readInt();
        this.username = in.readString();
        this.appusername = in.readString();
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

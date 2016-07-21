package vn.com.vng.zalopay.domain.model.redpacket;

import android.os.Parcel;

import java.util.List;

import vn.com.vng.zalopay.domain.model.AbstractData;

/**
 * Created by longlv on 15/07/2016.
 */
public class SentBundle extends AbstractData {
    public long bundleID;
    public String sendZaloPayID;
    public int type;
    public long createTime;
    public long lastOpenTime;
    public int totalLuck;
    public int numOfOpenedPakages;
    public int numOfPackages;
    public List<PackageInBundle> packages;

    public SentBundle(long bundleID, String sendZaloPayID, int type, long createTime, long lastOpenTime, int totalLuck, int numOfOpenedPakages, int numOfPackages) {
        this.bundleID = bundleID;
        this.sendZaloPayID = sendZaloPayID;
        this.type = type;
        this.createTime = createTime;
        this.lastOpenTime = lastOpenTime;
        this.totalLuck = totalLuck;
        this.numOfOpenedPakages = numOfOpenedPakages;
        this.numOfPackages = numOfPackages;
        this.packages = null;
    }

    public SentBundle(long bundleID, String sendZaloPayID, int type, long createTime, long lastOpenTime, int totalLuck, int numOfOpenedPakages, int numOfPackages, List<PackageInBundle> packages) {
        this.bundleID = bundleID;
        this.sendZaloPayID = sendZaloPayID;
        this.type = type;
        this.createTime = createTime;
        this.lastOpenTime = lastOpenTime;
        this.totalLuck = totalLuck;
        this.numOfOpenedPakages = numOfOpenedPakages;
        this.numOfPackages = numOfPackages;
        this.packages = packages;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.bundleID);
        dest.writeString(this.sendZaloPayID);
        dest.writeInt(this.type);
        dest.writeLong(this.createTime);
        dest.writeLong(this.lastOpenTime);
        dest.writeInt(this.totalLuck);
        dest.writeInt(this.numOfOpenedPakages);
        dest.writeInt(this.numOfPackages);
        dest.writeTypedList(this.packages);
    }

    protected SentBundle(Parcel in) {
        this.bundleID = in.readLong();
        this.sendZaloPayID = in.readString();
        this.type = in.readInt();
        this.createTime = in.readLong();
        this.lastOpenTime = in.readLong();
        this.totalLuck = in.readInt();
        this.numOfOpenedPakages = in.readInt();
        this.numOfPackages = in.readInt();
        this.packages = in.createTypedArrayList(PackageInBundle.CREATOR);
    }

    public static final Creator<SentBundle> CREATOR = new Creator<SentBundle>() {
        @Override
        public SentBundle createFromParcel(Parcel source) {
            return new SentBundle(source);
        }

        @Override
        public SentBundle[] newArray(int size) {
            return new SentBundle[size];
        }
    };
}

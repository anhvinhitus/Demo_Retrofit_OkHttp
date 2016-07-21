package vn.com.vng.zalopay.domain.model.redpacket;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import vn.com.vng.zalopay.domain.model.AbstractData;

/**
 * Created by longlv on 15/07/2016.
 */
public class SentBundle extends AbstractData {
    public long bundleID;
    public int type;
    public long createTime;
    public long lastOpenTime;
    public int totalLuck;
    public int numOfOpenedPakages;
    public int numOfPackages;
    public List<SentPackage> packages;

    public SentBundle(long bundleID, int type, long createTime, long lastOpenTime, int totalLuck, int numOfOpenedPakages, int numOfPackages) {
        this.bundleID = bundleID;
        this.type = type;
        this.createTime = createTime;
        this.lastOpenTime = lastOpenTime;
        this.totalLuck = totalLuck;
        this.numOfOpenedPakages = numOfOpenedPakages;
        this.numOfPackages = numOfPackages;
    }

    public SentBundle(long bundleID, int type, long createTime, long lastOpenTime, int totalLuck, int numOfOpenedPakages, int numOfPackages, List<SentPackage> packages) {
        this.bundleID = bundleID;
        this.type = type;
        this.createTime = createTime;
        this.lastOpenTime = lastOpenTime;
        this.totalLuck = totalLuck;
        this.numOfOpenedPakages = numOfOpenedPakages;
        this.numOfPackages = numOfPackages;
        this.packages = packages;
    }

    public SentBundle(Parcel in) {
        bundleID = in.readLong();
        type = in.readInt();
        createTime = in.readLong();
        lastOpenTime = in.readLong();
        totalLuck = in.readInt();
        numOfOpenedPakages = in.readInt();
        numOfPackages = in.readInt();
        in.readList(packages, null);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(bundleID);
        dest.writeInt(type);
        dest.writeLong(createTime);
        dest.writeLong(lastOpenTime);
        dest.writeInt(totalLuck);
        dest.writeInt(numOfOpenedPakages);
        dest.writeInt(numOfPackages);
        dest.writeList(packages);
    }

    public static final Parcelable.Creator<SentBundle> CREATOR = new Parcelable.Creator<SentBundle>() {
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

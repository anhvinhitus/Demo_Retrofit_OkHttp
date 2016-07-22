package vn.com.vng.zalopay.domain.model.redpacket;

import org.parceler.Parcel;

import java.util.List;

/**
 * Created by longlv on 15/07/2016.
 *
 */
@Parcel
public class SentBundle {
    public long bundleID;
    public String sendZaloPayID;
    public int type;
    public long createTime;
    public long lastOpenTime;
    public int totalLuck;
    public int numOfOpenedPakages;
    public int numOfPackages;
    public List<PackageInBundle> packages;

    public SentBundle() {

    }

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
}

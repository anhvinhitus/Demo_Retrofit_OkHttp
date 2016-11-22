package vn.com.vng.zalopay.domain.model.redpacket;

import java.util.List;

/**
 * Created by longlv on 15/07/2016.
 */
public class SentBundle {
    public long bundleID;
    public String sendZaloPayID;
    public int type;
    public long createTime;
    public long lastOpenTime;
    public int totalLuck;
    public int numOfOpenedPakages;
    public int numOfPackages;
    public String sendMessage;
    public int status;
    public List<PackageInBundle> packages;

    public SentBundle(long bundleID, String sendZaloPayID,
                      int type, long createTime, long lastOpenTime,
                      int totalLuck, int numOfOpenedPakages,
                      int numOfPackages, String sendMessage,
                      int status) {
        this.bundleID = bundleID;
        this.sendZaloPayID = sendZaloPayID;
        this.type = type;
        this.createTime = createTime;
        this.lastOpenTime = lastOpenTime;
        this.totalLuck = totalLuck;
        this.numOfOpenedPakages = numOfOpenedPakages;
        this.numOfPackages = numOfPackages;
        this.sendMessage = sendMessage;
        this.status = status;
        this.packages = null;
    }

    public SentBundle(long bundleID, String sendZaloPayID,
                      int type, long createTime, long lastOpenTime,
                      int totalLuck, int numOfOpenedPakages,
                      int numOfPackages, String sendMessage,
                      int status, List<PackageInBundle> packages) {
        this.bundleID = bundleID;
        this.sendZaloPayID = sendZaloPayID;
        this.type = type;
        this.createTime = createTime;
        this.lastOpenTime = lastOpenTime;
        this.totalLuck = totalLuck;
        this.numOfOpenedPakages = numOfOpenedPakages;
        this.numOfPackages = numOfPackages;
        this.sendMessage = sendMessage;
        this.status = status;
        this.packages = packages;
    }
}

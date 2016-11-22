package vn.com.vng.zalopay.domain.model.redpacket;

/**
 * Created by longlv on 13/07/2016.
 * Relate with RedPackageResponse which is data of request "sendbundle"
 */
public class SubmitOpenPackage {
    public long bundleID;
    public long packageID;
    public long zpTransID;

    public SubmitOpenPackage(long bundleID, long packageID, long zpTransID) {
        this.bundleID = bundleID;
        this.packageID = packageID;
        this.zpTransID = zpTransID;
    }
}

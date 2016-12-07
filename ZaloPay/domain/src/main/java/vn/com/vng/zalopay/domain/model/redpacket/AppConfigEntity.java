package vn.com.vng.zalopay.domain.model.redpacket;

/**
 * Created by longlv on 01/08/2016.
 * Mapping RedPacketAppInfoResponse
 */
public class AppConfigEntity {

    public long minAmountEach;
    public long maxTotalAmountPerBundle;
    public long maxPackageQuantity;
    public long maxCountHist;
    public long maxMessageLength;
    public long bundleExpiredTime;
    public long minDivideAmount;
    public long maxAmountPerPackage;

//    public AppConfigEntity(long bundleExpiredTime, int maxCountHist,
//                           int maxMessageLength, int maxPackageQuantity,
//                           long maxTotalAmountPerBundle, long minAmountEach,
//                           long minDivideAmount, long maxAmountPerPackage) {
//        this.bundleExpiredTime = bundleExpiredTime;
//        this.maxCountHist = maxCountHist;
//        this.maxMessageLength = maxMessageLength;
//        this.maxPackageQuantity = maxPackageQuantity;
//        this.maxTotalAmountPerBundle = maxTotalAmountPerBundle;
//        this.minAmountEach = minAmountEach;
//        this.minDivideAmount = minDivideAmount;
//        this.maxAmountPerPackage = maxAmountPerPackage;
//    }
}

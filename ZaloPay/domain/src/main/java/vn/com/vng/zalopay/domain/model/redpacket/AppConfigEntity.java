package vn.com.vng.zalopay.domain.model.redpacket;

/**
 * Created by longlv on 01/08/2016.
 * Mapping RedPacketAppInfoResponse
 */
public class AppConfigEntity {

    public long minAmounTeach;
    public long maxTotalAmountPerBundle;
    public int maxPackageQuantity;
    public int maxCountHist;
    public int maxMessageLength;
    public long bundleExpiredTime;
    public long minDivideAmount;

    public AppConfigEntity(long bundleExpiredTime, int maxCountHist,
                           int maxMessageLength, int maxPackageQuantity,
                           long maxTotalAmountPerBundle, long minAmounTeach,
                           long minDivideAmount) {
        this.bundleExpiredTime = bundleExpiredTime;
        this.maxCountHist = maxCountHist;
        this.maxMessageLength = maxMessageLength;
        this.maxPackageQuantity = maxPackageQuantity;
        this.maxTotalAmountPerBundle = maxTotalAmountPerBundle;
        this.minAmounTeach = minAmounTeach;
        this.minDivideAmount = minDivideAmount;
    }
}

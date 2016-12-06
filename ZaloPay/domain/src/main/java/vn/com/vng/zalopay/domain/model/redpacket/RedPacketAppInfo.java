package vn.com.vng.zalopay.domain.model.redpacket;

/**
 * Created by longlv on 01/08/2016.
 * Mapping RedPacketAppInfoResponse
 */
public class RedPacketAppInfo {
    public boolean isUpdateAppInfo;
    public String checksum;
    public long expiredTime;
    public AppConfigEntity appConfigEntity;
//
//    public RedPacketAppInfo(boolean isUpdateAppInfo, String checksum,
//                            long expiredTime, AppConfigEntity appConfigEntity) {
//        this.isUpdateAppInfo = isUpdateAppInfo;
//        this.checksum = checksum;
//        this.expiredTime = expiredTime;
//        this.appConfigEntity = appConfigEntity;
//    }
}

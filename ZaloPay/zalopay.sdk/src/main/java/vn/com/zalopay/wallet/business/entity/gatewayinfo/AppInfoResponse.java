package vn.com.zalopay.wallet.business.entity.gatewayinfo;

import java.util.List;
import java.util.Map;

import vn.com.zalopay.wallet.business.entity.base.BaseResponse;

public class AppInfoResponse extends BaseResponse {
    public boolean isupdateappinfo;
    public String checksum;
<<<<<<< HEAD:ZaloPay/zalopay.sdk/src/main/java/vn/com/zalopay/wallet/business/entity/gatewayinfo/DAppInfoResponse.java
    public List<DChannelMapApp> transtypepmcs;
=======
    public Map<Integer,List<MiniPmcTransType>> pmctranstypes;
>>>>>>> 9fd9a35... [SDK] Apply app info v1:ZaloPay/zalopay.sdk/src/main/java/vn/com/zalopay/wallet/business/entity/gatewayinfo/AppInfoResponse.java
    public DAppInfo info;
    public long expiredtime;

}

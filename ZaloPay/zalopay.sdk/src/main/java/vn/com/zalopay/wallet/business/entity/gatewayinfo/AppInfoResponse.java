package vn.com.zalopay.wallet.business.entity.gatewayinfo;

import java.util.List;

import vn.com.zalopay.wallet.business.entity.base.BaseResponse;

public class AppInfoResponse extends BaseResponse {
    public boolean isupdateappinfo;
<<<<<<< HEAD
    public String checksum;
    public Map<Integer, List<MiniPmcTransType>> pmctranstypes;
=======
    public String appinfochecksum;
    public List<MiniPmcTransTypeResponse> pmctranstypes;
>>>>>>> c78224b... [SDK] Update app info v1
    public DAppInfo info;
    public long expiredtime;
}

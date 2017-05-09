package vn.com.zalopay.wallet.business.entity.gatewayinfo;

import java.util.List;
import java.util.Map;

import vn.com.zalopay.wallet.business.entity.base.BaseResponse;

public class AppInfoResponse extends BaseResponse {
    public boolean isupdateappinfo;
    public String checksum;
    public Map<Integer,List<MiniPmcTransType>> pmctranstypes;
    public DAppInfo info;
    public long expiredtime;

}

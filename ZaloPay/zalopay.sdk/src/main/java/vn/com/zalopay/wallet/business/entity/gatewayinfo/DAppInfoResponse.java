package vn.com.zalopay.wallet.business.entity.gatewayinfo;

import java.util.List;

import vn.com.zalopay.wallet.business.entity.base.BaseResponse;

public class DAppInfoResponse extends BaseResponse {
    public boolean isupdateappinfo;
    public String checksum;
    public List<DChannelMapApp> transtypepmcs;
    public DAppInfo info;
    public long expiredtime;

}

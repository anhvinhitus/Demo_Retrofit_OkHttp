package vn.com.zalopay.wallet.business.entity.gatewayinfo;

import java.util.List;

import vn.com.zalopay.wallet.business.entity.base.BaseResponse;

public class AppInfoResponse extends BaseResponse {
    public boolean isupdateappinfo;
    public String appinfochecksum;
    public List<MiniPmcTransTypeResponse> pmctranstypes;
    public AppInfo info;
    public long expiredtime;

    public boolean hasTranstypes() {
        return pmctranstypes != null && !pmctranstypes.isEmpty();
    }

    public boolean needUpdateAppInfo() {
        return returncode == 1 && isupdateappinfo && info != null;
    }
}

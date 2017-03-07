package vn.com.zalopay.wallet.business.entity.gatewayinfo;

import java.util.List;

import vn.com.zalopay.wallet.business.entity.base.BaseResponse;

public class DPlatformInfo extends BaseResponse {
    public String platforminfochecksum = null;

    public List<DMappedCard> cardinfos = null;
    public String cardinfochecksum = null;

    public List<DBankAccount> bankaccounts = null;
    public String bankaccountchecksum = null;

    public List<DChannelMapApp> transtypepmcs = null;

    public DAppInfo info = null;

    public boolean isupdateresource = false;
    public DSDKResource resource = null;

    public boolean forceappupdate = false;
    public String forceupdatemessage = null;

    public boolean isenabledeposit = true;

    public boolean ismaintainwithdraw = false;

    public Long maintainwithdrawfrom = 0L;
    public Long maintainwithdrawto = 0L;

    public String newestappversion = null;

    public List<DBanner> bannerresources = null;

    public List<Integer> approvedinsideappids = null;

    public long expiredtime = 600000;
}

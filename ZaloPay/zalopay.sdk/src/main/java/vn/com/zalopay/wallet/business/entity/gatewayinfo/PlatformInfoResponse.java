package vn.com.zalopay.wallet.business.entity.gatewayinfo;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import vn.com.zalopay.wallet.business.entity.base.BaseResponse;

public class PlatformInfoResponse extends BaseResponse {

    @SerializedName("platforminfochecksum")
    public String platforminfochecksum = null;

    @SerializedName("cardinfos")
    public List<MapCard> cardinfos = null;

    @SerializedName("cardinfochecksum")
    public String cardinfochecksum = null;

    @SerializedName("bankaccounts")
    public List<BankAccount> bankaccounts = null;

    @SerializedName("bankaccountchecksum")
    public String bankaccountchecksum = null;

    @SerializedName("isupdateresource")
    public boolean isupdateresource = false;

    @SerializedName("resource")
    public SDKResource resource = null;

    @SerializedName("forceappupdate")
    public boolean forceappupdate = false;

    @SerializedName("forceupdatemessage")
    public String forceupdatemessage = null;

    @SerializedName("isenabledeposit")
    public boolean isenabledeposit = true;

    @SerializedName("ismaintainwithdraw")
    public boolean ismaintainwithdraw = false;

    @SerializedName("maintainwithdrawfrom")
    public Long maintainwithdrawfrom = 0L;

    @SerializedName("maintainwithdrawto")
    public Long maintainwithdrawto = 0L;

    @SerializedName("newestappversion")
    public String newestappversion = null;

    @SerializedName("expiredtime")
    public long expiredtime = 600000;
}

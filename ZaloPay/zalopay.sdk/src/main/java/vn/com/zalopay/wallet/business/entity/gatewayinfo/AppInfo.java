package vn.com.zalopay.wallet.business.entity.gatewayinfo;


import com.google.gson.annotations.SerializedName;

public class AppInfo {
    @SerializedName("appname")
    public String appname;
    @SerializedName("appid")
    public long appid = Long.MIN_VALUE;
    @SerializedName("status")
    public int status = -1;
    @SerializedName("expriretime")
    public long expriretime;
    @SerializedName("redirect_url")
    public String redirect_url;

    public boolean isAllow() {
        return status == 1;
    }
}

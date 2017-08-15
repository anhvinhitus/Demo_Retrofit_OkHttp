package vn.com.zalopay.wallet.business.entity.gatewayinfo;

import com.google.gson.annotations.SerializedName;

public class SDKResource {
    @SerializedName("appversion")
    public String appversion;
    @SerializedName("rsurl")
    public String rsurl;
    @SerializedName("rsversion")
    public String rsversion;
}

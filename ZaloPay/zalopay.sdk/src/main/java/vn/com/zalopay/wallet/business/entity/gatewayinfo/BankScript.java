package vn.com.zalopay.wallet.business.entity.gatewayinfo;

import com.google.gson.annotations.SerializedName;

public class BankScript {
    @SerializedName("url")
    public String url;
    @SerializedName("autoJs")
    public String autoJs;
    @SerializedName("hitJs")
    public String hitJs;
    @SerializedName("eventID")
    public int eventID;
    @SerializedName("pageCode")
    public String pageCode;
}

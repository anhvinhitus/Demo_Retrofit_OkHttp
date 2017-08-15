package vn.com.zalopay.wallet.business.entity.base;

import com.google.gson.annotations.SerializedName;

public class SecurityResponse {

    @SerializedName("redirecturl")
    public String redirecturl;

    @SerializedName("actiontype")
    public int actiontype;
}

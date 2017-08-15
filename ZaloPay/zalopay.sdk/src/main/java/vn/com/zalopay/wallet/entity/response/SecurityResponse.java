package vn.com.zalopay.wallet.entity.response;

import com.google.gson.annotations.SerializedName;

public class SecurityResponse {

    @SerializedName("redirecturl")
    public String redirecturl;

    @SerializedName("actiontype")
    public int actiontype;
}

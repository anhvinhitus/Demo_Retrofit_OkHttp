package vn.com.zalopay.wallet.business.entity.base;

import com.google.gson.annotations.SerializedName;

public class BaseResponse {
    @SerializedName("returncode")
    public int returncode = 0;

    @SerializedName("returnmessage")
    public String returnmessage = null;

    @SerializedName("accesstoken")
    public String accesstoken = null;
}

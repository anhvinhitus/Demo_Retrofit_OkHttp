package vn.com.zalopay.wallet.entity.response;

import com.google.gson.annotations.SerializedName;

public class BaseResponse {
    @SerializedName("returncode")
    public int returncode = 0;

    @SerializedName("returnmessage")
    public String returnmessage = null;

    @SerializedName("accesstoken")
    public String accesstoken = null;
}

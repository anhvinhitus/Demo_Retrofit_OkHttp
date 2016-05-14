package vn.com.vng.zalopay.data.api.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by AnhHieu on 4/25/16.
 */
public class LoginResponse extends BaseResponse {

    @SerializedName("accesstoken")
    public String accesstoken;

    @SerializedName("expirein")
    public long expirein;

    @SerializedName("userid")
    public long userid;
}

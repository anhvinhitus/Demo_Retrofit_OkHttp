package vn.com.vng.zalopay.data.api.response;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

/**
 * Created by AnhHieu on 4/25/16.
 * Response object for login
 */
public class LoginResponse extends BaseResponse {

    @SerializedName("accesstoken")
    public String accesstoken;

    @SerializedName("expirein")
    public long expirein;

    @SerializedName("userid")
    public String userid;

    @SerializedName("zalopayname")
    public String zalopayname;

    @SerializedName("profilelevel")
    public int profilelevel;

    @SerializedName("phonenumber")
    public long phonenumber;

    @SerializedName("profilelevelpermisssion")
    public JsonElement permission;
}

package vn.com.vng.zalopay.data.api.response;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

/**
 * Created by AnhHieu on 7/19/16.
 *
 */
public class UserProfileLevelResponse extends BaseResponse {
    @SerializedName("email")
    public String email;

    @SerializedName("identity")
    public String identityNumber;

    @SerializedName("phonenumber")
    public long phonenumber;

    @SerializedName("zaloid")
    public String zaloid;

    @SerializedName("avatar")
    public String avatar;

    @SerializedName("profilelevel")
    public int profilelevel;

    @SerializedName("profilelevelpermisssion")
    public JsonElement permisstion;

    @SerializedName("zalopayname")
    public String zalopayname;
}

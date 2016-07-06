package vn.com.vng.zalopay.data.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import vn.com.vng.zalopay.domain.model.ProfilePermission;

/**
 * Created by AnhHieu on 4/25/16.
 */
public class LoginResponse extends BaseResponse {

    @SerializedName("accesstoken")
    public String accesstoken;

    @SerializedName("expirein")
    public long expirein;

    @SerializedName("userid")
    public String userid;

    @SerializedName("profilelevel")
    public int profilelevel;

    @SerializedName("phonenumber")
    public long phonenumber;

    @SerializedName("profilelevelpermisssion")
    public List<ProfilePermission.Permission> profilelevelpermisssion;
}

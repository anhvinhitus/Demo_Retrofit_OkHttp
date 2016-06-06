package vn.com.vng.zalopay.data.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import vn.com.vng.zalopay.domain.model.ProfilePermisssion;

/**
 * Created by longlv on 03/06/2016.
 */
public class UpdateProfileResponse extends BaseResponse {

    @SerializedName("profilelevel")
    public int profilelevel;

    @SerializedName("profilelevelpermisssion")
    public List<ProfilePermisssion.Permission> profilePermisssions;
}

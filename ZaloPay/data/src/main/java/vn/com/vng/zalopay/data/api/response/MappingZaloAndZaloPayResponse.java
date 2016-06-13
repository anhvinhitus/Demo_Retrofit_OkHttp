package vn.com.vng.zalopay.data.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import vn.com.vng.zalopay.domain.model.ProfilePermisssion;

/**
 * Created by AnhHieu on 4/25/16.
 */
public class MappingZaloAndZaloPayResponse extends BaseResponse {

    @SerializedName("userid")
    public String userid;

    @SerializedName("phonenumber")
    public String phonenumber;
}

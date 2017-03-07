package vn.com.vng.zalopay.data.api.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by AnhHieu on 5/24/16.
 */
public class GetMerchantUserInfoResponse extends BaseResponse {

    @SerializedName("displayname")
    public String displayname;

    @SerializedName("birthdate")
    public String birthdate;

    @SerializedName("usergender")
    public long usergender;

    @SerializedName("muid")
    public String muid;

    @SerializedName("maccesstoken")
    public String maccesstoken;

}

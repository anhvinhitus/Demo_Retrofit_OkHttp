package vn.com.vng.zalopay.data.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by AnhHieu on 9/21/16.
 * Response data class for API listmerchantuserinfo
 */
public class ListMUIResponse extends BaseResponse {

    @SerializedName("displayname")
    public String displayname;

    @SerializedName("birthdate")
    public String birthdate;

    @SerializedName("usergender")
    public long usergender;

    @SerializedName("listmerchantuserinfo")
    public List<MerchantUserSubInfo> listmerchantuserinfo;

    public class MerchantUserSubInfo {
        @SerializedName("appid")
        public long appid;
        @SerializedName("muid")
        public String muid;
        @SerializedName("maccesstoken")
        public String maccesstoken;
    }
}

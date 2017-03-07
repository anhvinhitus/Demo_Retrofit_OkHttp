package vn.com.vng.zalopay.data.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by AnhHieu on 9/21/16.
 * *
 */

public class ListMerchantUserInfoResponse extends BaseResponse {

    @SerializedName("displayname")
    public String displayname;

    @SerializedName("birthdate")
    public String birthdate;

    @SerializedName("usergender")
    public int usergender;

    @SerializedName("listmerchantuserinfo")
    public List<ListMerchantUserInfo> listmerchantuserinfo;

    public class ListMerchantUserInfo {
        @SerializedName("appid")
        public long appid;
        @SerializedName("muid")
        public String muid;
        @SerializedName("maccesstoken")
        public String maccesstoken;
    }
}

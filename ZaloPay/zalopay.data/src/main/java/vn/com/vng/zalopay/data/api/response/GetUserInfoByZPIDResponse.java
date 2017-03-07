package vn.com.vng.zalopay.data.api.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by huuhoa on 8/27/16.
 * Mapping for getuserinfobyzalopayid
 */
public class GetUserInfoByZPIDResponse extends BaseResponse {
    @SerializedName("phonenumber")
    public long phoneNumber;

    @SerializedName("displayname")
    public String displayName;

    @SerializedName("avatar")
    public String avatar;

    @SerializedName("zalopayname")
    public String zalopayname;
}

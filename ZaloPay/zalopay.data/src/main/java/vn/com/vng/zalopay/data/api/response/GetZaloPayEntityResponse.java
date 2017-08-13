package vn.com.vng.zalopay.data.api.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by hieuvm on 8/11/17.
 * *
 */

public class GetZaloPayEntityResponse extends BaseResponse {
    @SerializedName("zaloid")
    public String zaloid;
    @SerializedName("userid")
    public String userid;
    @SerializedName("phonenumber")
    public String phonenumber;
    @SerializedName("zalopayname")
    public String zalopayname;
    @SerializedName("status")
    public long status; // = 1 exist
    @SerializedName("displayname")
    public String displayName;
    @SerializedName("avatar")
    public String avatar;
}

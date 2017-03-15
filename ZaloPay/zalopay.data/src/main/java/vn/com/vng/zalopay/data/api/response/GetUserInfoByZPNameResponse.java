package vn.com.vng.zalopay.data.api.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by longlv on 12/08/2016.
 * Mapping /um/getuseridbyzalopayname
 */
public class GetUserInfoByZPNameResponse extends BaseResponse {

    @SerializedName("userid")
    public String userid;

    @SerializedName("phonenumber")
    public long phoneNumber;

    @SerializedName("displayname")
    public String displayName;

    @SerializedName("avatar")
    public String avatar;
}
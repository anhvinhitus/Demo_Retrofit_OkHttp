package vn.com.vng.zalopay.data.api.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by longlv on 12/08/2016.
 * Mapping /um/getuseridbyzalopay
 */
public class GetUserInfoByZPNameResponse extends BaseResponse {

    @SerializedName("userid")
    public String userid;

    @SerializedName("phonenumber")
    public String phoneNumber;

    @SerializedName("displayname")
    public String displayName;
}

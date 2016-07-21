package vn.com.vng.zalopay.data.api.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by AnhHieu on 7/19/16.
 */
public class UserProfileLevelResponse extends UpdateProfileResponse {
    @SerializedName("email")
    public String email;

    @SerializedName("identity")
    public String identityNumber;
}

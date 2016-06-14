package vn.com.vng.zalopay.data.api.response;

import com.google.gson.annotations.SerializedName;

import vn.com.vng.zalopay.data.NetworkError;

/**
 * Created by AnhHieu on 3/24/16.
 */
public class BaseResponse {

    @SerializedName("returncode")
    public int err;

    @SerializedName("returnmessage")
    public String message;

    public boolean isSuccessfulResponse() {
        return err == 1;
    }

    public boolean isSessionExpired() {
        return err == NetworkError.UM_TOKEN_NOT_FOUND || err == NetworkError.UM_TOKEN_EXPIRE || err == NetworkError.TOKEN_INVALID;
    }
}

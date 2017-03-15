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

    @SerializedName("accesstoken")
    public String accesstoken;

    public boolean isSuccessfulResponse() {
        return err == NetworkError.SUCCESSFUL;
    }

    public boolean isSessionExpired() {
        return err == NetworkError.UM_TOKEN_NOT_FOUND || err == NetworkError.UM_TOKEN_EXPIRE
                || err == NetworkError.TOKEN_INVALID;
    }

    public boolean isServerMaintain() {
        return err == NetworkError.SERVER_MAINTAIN;
    }

    public boolean isInvitationCode() {
        return err == NetworkError.INVITATION_CODE_ERROR;
    }

    public boolean isAccountSuspended() {
        return err == NetworkError.ZPW_ACCOUNT_SUSPENDED
                || err == NetworkError.USER_IS_LOCKED;
    }
}
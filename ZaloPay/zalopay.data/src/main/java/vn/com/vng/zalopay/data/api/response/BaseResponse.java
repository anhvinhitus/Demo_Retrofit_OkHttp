package vn.com.vng.zalopay.data.api.response;

import com.google.gson.annotations.SerializedName;

import vn.com.vng.zalopay.data.ServerErrorMessage;

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
        return err == ServerErrorMessage.SUCCESSFUL;
    }

    public boolean isSessionExpired() {
        return err == ServerErrorMessage.UM_TOKEN_NOT_FOUND || err == ServerErrorMessage.UM_TOKEN_EXPIRE
                || err == ServerErrorMessage.TOKEN_INVALID;
    }

    public boolean isServerMaintain() {
        return err == ServerErrorMessage.SERVER_MAINTAIN;
    }

    public boolean isInvitationCode() {
        return err == ServerErrorMessage.INVITATION_CODE_ERROR;
    }

    public boolean isAccountSuspended() {
        return err == ServerErrorMessage.ZPW_ACCOUNT_SUSPENDED
                || err == ServerErrorMessage.USER_IS_LOCKED;
    }
}

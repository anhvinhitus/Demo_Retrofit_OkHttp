package vn.com.vng.zalopay.data.exception;

import vn.com.vng.zalopay.data.api.response.BaseResponse;

/**
 * Created by AnhHieu on 7/5/16.
 */
public class InvitationCodeException extends BodyException {

    public BaseResponse response;

    public InvitationCodeException(int error, BaseResponse response) {
        super(error);
        this.response = response;
    }

    public InvitationCodeException(int error, String message) {
        super(error, message);
    }
}

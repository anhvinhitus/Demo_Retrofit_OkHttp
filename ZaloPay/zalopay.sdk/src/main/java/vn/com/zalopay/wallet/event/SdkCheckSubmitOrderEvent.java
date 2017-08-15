package vn.com.zalopay.wallet.event;

import vn.com.zalopay.wallet.entity.response.StatusResponse;

/**
 * Created by chucvv on 7/19/17.
 */

public class SdkCheckSubmitOrderEvent {
    public StatusResponse response;

    public SdkCheckSubmitOrderEvent(StatusResponse response) {
        this.response = response;
    }
}

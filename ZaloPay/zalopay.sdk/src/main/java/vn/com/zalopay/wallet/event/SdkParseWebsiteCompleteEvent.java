package vn.com.zalopay.wallet.event;

import vn.com.zalopay.wallet.business.entity.base.BaseResponse;

/**
 * Created by chucvv on 7/20/17.
 */

public class SdkParseWebsiteCompleteEvent {
    public BaseResponse response;

    public SdkParseWebsiteCompleteEvent(BaseResponse response) {
        this.response = response;
    }
}

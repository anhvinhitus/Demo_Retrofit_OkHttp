package vn.com.vng.zalopay.data.api.response.redpacket;

import vn.com.vng.zalopay.data.api.response.BaseResponse;

/**
 * Created by longlv on 18/07/2016.
 * Data of getBundleStatus request
 */
public class BundleStatusResponse extends BaseResponse {
    /*
        "NOTE: bundleStatus == 3 (AVAILABLE) -> SendBundle thành công
        1: INIT
        2: PACKAGE_GEN
        3: AVAILABLE
        4: REFUND
    */

    public int bundleStatus;

}

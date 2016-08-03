package vn.com.vng.zalopay.data.api.response.redpacket;

import com.google.gson.annotations.SerializedName;

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

    public enum BundleStatusEnum {
        UNKNOWN(0),
        INIT(1),
        PACKAGE_GEN(2),
        AVAILABLE(3),
        REFUND(4);

        int value;

        BundleStatusEnum(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    @SerializedName("bundlestatus")
    public int bundleStatus;

}

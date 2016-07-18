package vn.com.vng.zalopay.data.api.response.redpackage;

import com.google.gson.annotations.SerializedName;

import vn.com.vng.zalopay.data.api.response.GetOrderResponse;

/**
 * Created by longlv on 13/07/2016.
 */
public class BundleOrderResponse extends GetOrderResponse {

    @SerializedName("bundleid")
    public long bundleID;
}

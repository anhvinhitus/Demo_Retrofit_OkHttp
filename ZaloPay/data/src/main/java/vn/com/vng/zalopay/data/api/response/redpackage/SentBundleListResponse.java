package vn.com.vng.zalopay.data.api.response.redpackage;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import vn.com.vng.zalopay.data.api.response.BaseResponse;

/**
 * Created by longlv on 15/07/2016.
 */
public class SentBundleListResponse extends BaseResponse {

    @SerializedName("totalOfSentAmount")
    public int totalOfSentAmount;
    @SerializedName("bundles")
    public List<SentBundleResponse> bundleResponseList;
}

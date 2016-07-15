package vn.com.vng.zalopay.data.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by longlv on 15/07/2016.
 */
public class SentBundleResponse extends BaseResponse {

    @SerializedName("totalOfSentAmount")
    public int totalOfSentAmount;
    @SerializedName("bundles")
    public List<BundleResponse> bundleResponseList;
}

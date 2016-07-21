package vn.com.vng.zalopay.data.api.response.redpacket;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import vn.com.vng.zalopay.data.api.response.BaseResponse;

/**
 * Created by longlv on 16/07/2016.
 * Data of getPackageInBundleList request
 */
public class SentPackageInBundleResponse extends BaseResponse {

    @SerializedName("packages")
    public List<SentPackageResponse> packageResponses;
}

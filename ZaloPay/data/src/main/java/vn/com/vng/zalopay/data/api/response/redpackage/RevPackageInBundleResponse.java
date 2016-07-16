package vn.com.vng.zalopay.data.api.response.redpackage;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import vn.com.vng.zalopay.data.api.response.BaseResponse;

/**
 * Created by longlv on 16/07/2016.
 */
public class RevPackageInBundleResponse extends BaseResponse {

    @SerializedName("packages")
    List<ReceivePackageResponse> receivePackageResponses;
}

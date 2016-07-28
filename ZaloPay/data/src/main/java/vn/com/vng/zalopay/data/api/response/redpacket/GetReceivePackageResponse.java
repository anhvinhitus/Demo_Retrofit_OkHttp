package vn.com.vng.zalopay.data.api.response.redpacket;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import vn.com.vng.zalopay.data.api.response.BaseResponse;

/**
 * Created by longlv on 16/07/2016.
 * Data of getRevPackageList request
 */
public class GetReceivePackageResponse extends BaseResponse {
    @SerializedName("totalofrevamount")
    public long totalOfRevAmount;

    @SerializedName("totalofrevpackage")
    public int totalOfRevPackage;

    @SerializedName("totalofluckiestdraw")
    public int numOfLuckiestDraw;

    @SerializedName("revpackagelist")
    public List<ReceivePackageResponse> receivePackageResponses;
}

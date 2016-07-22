package vn.com.vng.zalopay.data.api.response.redpacket;

import com.google.gson.annotations.SerializedName;

import vn.com.vng.zalopay.data.api.response.BaseResponse;

/**
 * Created by longlv on 15/07/2016.
 */
public class ReceivePackageResponse extends BaseResponse {
    @SerializedName("packageID")
    public long packageid;
    @SerializedName("bundleID")
    public long bundleid;
    @SerializedName("revZaloPayID")
    public String revzalopayid;
    @SerializedName("sendZaloPayID")
    public String sendzalopayid;
    @SerializedName("sendFullName")
    public String sendfullname;
    @SerializedName("amount")
    public long amount;
    @SerializedName("openedTime")
    public long openedtime;
}

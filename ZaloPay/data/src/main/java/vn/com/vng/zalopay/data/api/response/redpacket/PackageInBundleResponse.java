package vn.com.vng.zalopay.data.api.response.redpacket;

import com.google.gson.annotations.SerializedName;

import vn.com.vng.zalopay.data.api.response.BaseResponse;

/**
 * Created by longlv on 15/07/2016.
 *
 */
public class PackageInBundleResponse extends BaseResponse {
    @SerializedName("packageID")
    public long packageid;
    @SerializedName("bundleID")
    public long bundleid;
    @SerializedName("revZaloPayID")
    public String revzalopayid;
    @SerializedName("revZaloID")
    public long revzaloid;
    @SerializedName("revFullName")
    public String revfullname;
    @SerializedName("revAvatarURL")
    public String revavatarurl;
    @SerializedName("openTime")
    public long opentime;
    @SerializedName("amount")
    public long amount;
    @SerializedName("sendMessage")
    public String sendmessage;
    @SerializedName("isLuckiest")
    public boolean isluckiest;
}

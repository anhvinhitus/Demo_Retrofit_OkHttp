package vn.com.vng.zalopay.data.api.response.redpacket;

import com.google.gson.annotations.SerializedName;

import vn.com.vng.zalopay.data.api.response.BaseResponse;

/**
 * Created by longlv on 15/07/2016.
 *
 */
public class PackageInBundleResponse extends BaseResponse {
    @SerializedName("packageid")
    public long packageid;

    @SerializedName("bundleid")
    public long bundleid;

    @SerializedName("revzalopayid")
    public String revzalopayid;

    @SerializedName("revzaloid")
    public long revzaloid;

    @SerializedName("revfullname")
    public String revfullname;

    @SerializedName("revavatarurl")
    public String revavatarurl;

    @SerializedName("opentime")
    public long opentime;

    @SerializedName("amount")
    public long amount;

    @SerializedName("sendmessage")
    public String sendmessage;

    @SerializedName("isluckiest")
    public boolean isluckiest;
}

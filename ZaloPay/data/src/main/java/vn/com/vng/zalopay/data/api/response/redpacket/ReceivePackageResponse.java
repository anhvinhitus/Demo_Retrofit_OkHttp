package vn.com.vng.zalopay.data.api.response.redpacket;

import com.google.gson.annotations.SerializedName;

import vn.com.vng.zalopay.data.api.response.BaseResponse;

/**
 * Created by longlv on 15/07/2016.
 *
 */
public class ReceivePackageResponse extends BaseResponse {
    @SerializedName("packageid")
    public long packageid;

    @SerializedName("bundleid")
    public long bundleid;

    @SerializedName("revzalopayid")
    public String revzalopayid;

    @SerializedName("sendzalopayid")
    public String sendzalopayid;

    @SerializedName("sendfullname")
    public String sendfullname;

    @SerializedName("amount")
    public long amount;

    @SerializedName("openedtime")
    public long openedtime;

    @SerializedName("isluckiest")
    public int isluckiest;

    @SerializedName("sendmessage")
    public String sendmessage;

    @SerializedName("avatarofsender")
    public String avatarofsender;

    @SerializedName("createtime")
    public long createtime;

}

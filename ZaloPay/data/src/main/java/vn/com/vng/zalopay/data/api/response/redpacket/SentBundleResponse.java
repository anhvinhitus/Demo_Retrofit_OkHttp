package vn.com.vng.zalopay.data.api.response.redpacket;

import com.google.gson.annotations.SerializedName;

import vn.com.vng.zalopay.data.api.response.BaseResponse;

/**
 * Created by longlv on 15/07/2016.
 */
public class SentBundleResponse extends BaseResponse {
    @SerializedName("bundleid")
    public long bundleid;

    @SerializedName("sendzalopayid")
    public String sendzalopayid;

    @SerializedName("type")
    public int type;

    @SerializedName("createtime")
    public long createtime;

    @SerializedName("lastopentime")
    public long lastopentime;

    @SerializedName("totalluck")
    public int totalluck;

    @SerializedName("numofopenedpackages")
    public int numofopenedpakages;

    @SerializedName("numofpackages")
    public int numofpackages;
}

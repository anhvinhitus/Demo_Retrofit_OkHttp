package vn.com.vng.zalopay.data.api.response.redpacket;

import com.google.gson.annotations.SerializedName;

import vn.com.vng.zalopay.data.api.response.BaseResponse;

/**
 * Created by longlv on 15/07/2016.
 */
public class SentBundleResponse extends BaseResponse {
    @SerializedName("bundleID")
    public long bundleid;
    @SerializedName("sendZaloPayID")
    public String sendzalopayid;
    @SerializedName("type")
    public int type;
    @SerializedName("createTime")
    public long createtime;
    @SerializedName("lastOpenTime")
    public long lastopentime;
    @SerializedName("totalLuck")
    public int totalluck;
    @SerializedName("numOfOpenedPackages")
    public int numofopenedpakages;
    @SerializedName("numOfPackages")
    public int numofpackages;
}

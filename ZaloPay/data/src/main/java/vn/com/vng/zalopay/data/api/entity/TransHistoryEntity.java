package vn.com.vng.zalopay.data.api.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by AnhHieu on 5/4/16.
 */
public class TransHistoryEntity {

    @SerializedName("userid")
    public String userid;

    @SerializedName("transid")
    public long transid;

    @SerializedName("appid")
    public long appid;

    @SerializedName("appuser")
    public String appuser;

    @SerializedName("platform")
    public String platform;

    @SerializedName("description")
    public String description;

    @SerializedName("pmcid")
    public int pmcid;

    @SerializedName("reqdate")
    public long reqdate;

    @SerializedName("grossamount")
    public int grossamount;

    @SerializedName("netamount")
    public int netamount;

    @SerializedName("type")
    public int type;
    
}

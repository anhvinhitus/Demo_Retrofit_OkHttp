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

    @SerializedName("userchargeamt")
    public int userchargeamt;

    @SerializedName("amount")
    public int amount;

    @SerializedName("userfeeamt")
    public int userfeeamt;

    @SerializedName("type")
    public int type;
    
}

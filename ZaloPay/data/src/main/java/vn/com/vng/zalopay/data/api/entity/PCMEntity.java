package vn.com.vng.zalopay.data.api.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by AnhHieu on 4/28/16.
 */
public class PCMEntity {

    @SerializedName("pmcid")
    public long pmcid;

    @SerializedName("pmcname")
    public String pmcname;

    @SerializedName("status")
    public long status;

    @SerializedName("minvalue")
    public long minvalue;

    @SerializedName("maxvalue")
    public long maxvalue;

    @SerializedName("feerate")
    public float feerate;

    @SerializedName("minxfee")
    public long minxfee;

    @SerializedName("feecaltype")
    public String feecaltype;

  /*  "pmcid":36,
            "pmcname":"Thẻ Visa/Master/JCB",
            "status":1,
            "minvalue":10000,
            "maxvalue":10000,
            "feerate":0,
            "minfee":0,
            "feecaltype":"SUM"
            */


}

package vn.com.vng.zalopay.data.api.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by AnhHieu on 7/28/16.
 */
public class PermissionEntity {

    @SerializedName("transtype")
    public int transtype;

    @SerializedName("pmcid")
    public long pmcid;

    @SerializedName("profilelevel")
    public int profilelevel;

    @SerializedName("allow")
    public boolean allow;

    @SerializedName("requireotp")
    public boolean requireotp;

    @SerializedName("requirepin")
    public boolean requirepin;
}

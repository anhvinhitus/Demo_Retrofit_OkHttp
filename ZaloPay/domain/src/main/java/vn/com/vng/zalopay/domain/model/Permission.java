package vn.com.vng.zalopay.domain.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by AnhHieu on 7/28/16.
 */
@org.parceler.Parcel
public class Permission {
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

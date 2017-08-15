package vn.com.zalopay.wallet.business.entity.staticconfig.page;

import com.google.gson.annotations.SerializedName;

public class StaticView {
    @SerializedName("id")
    public String id;
    @SerializedName("value")
    public String value;
    @SerializedName("enable")
    public boolean enable = true;
}

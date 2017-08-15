package vn.com.zalopay.wallet.entity.config;

import com.google.gson.annotations.SerializedName;

public class DPage {
    @SerializedName("pageName")
    public String pageName;
    @SerializedName("staticView")
    public StaticViewGroup staticView;
    @SerializedName("dynamicView")
    public DynamicViewGroup dynamicView;
}

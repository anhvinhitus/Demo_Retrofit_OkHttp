package vn.com.zalopay.wallet.business.entity.staticconfig;

import com.google.gson.annotations.SerializedName;

import vn.com.zalopay.wallet.business.entity.staticconfig.page.DynamicViewGroup;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.StaticViewGroup;

public class DPage {
    @SerializedName("pageName")
    public String pageName;
    @SerializedName("staticView")
    public StaticViewGroup staticView;
    @SerializedName("dynamicView")
    public DynamicViewGroup dynamicView;
}

package vn.com.vng.zalopay.domain.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by longlv on 2/13/17.
 * Parse file config in resource app 1.
 */

public class Config {

    @SerializedName("general")
    public General mGeneral;
    
    @SerializedName("search")
    public Search mSearch;

    @SerializedName("zpc")
    public Zpc mZpc;

    @SerializedName("api")
    public Api mApi;

    @SerializedName("withdraw")
    public Withdraw mWithdraw;

    @SerializedName("webapp")
    public WebApp mWebApp;

    @SerializedName("tab_me")
    public TabMe mTabMe;

    @SerializedName("notification")
    public Notification mNotification;

    @SerializedName("tab_home")
    public TabHome mTabHome;

    @SerializedName("promotion")
    public Promotion mPromotion;
}

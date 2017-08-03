package vn.com.vng.zalopay.domain.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by khattn on 3/14/17.
 * Parse file config in resource app 1.
 */

public class InsideApp {

    public static class Constants {
        public static final int WITHDRAW = 1;
        public static final int RECHARGE = 2;
        public static final int LINK_CARD = 3;
        public static final int PAY_QR_CODE = 4;
        public static final int HISTORY = 5;
        public static final int SUPPORT_CENTER = 6;
        public static final int INFORMATION = 7;
        public static final int PROFILE = 8;
        public static final int NOTIFICATION = 9;
        public static final int BALANCE = 10;
        public static final int CHANGE_PIN = 11;
        public static final int PROTECT_ACCOUNT = 12;
        public static final int TRANSFERS = 13;
        public static final int  RECEIVE_MONEY = 14;
    }

    public long appId;

    @SerializedName("inside_app_id")
    public long insideAppId;

    @SerializedName("app_type")
    public long appType;

    @SerializedName("app_name")
    public String appName;

    @SerializedName("icon_name")
    public String iconName;

    @SerializedName("icon_color")
    public String iconColor;

    @SerializedName("module_name")
    public String moduleName;

    public String webUrl;

    public InsideApp(long appId, long appType, String appName,
                     String iconName, String iconColor, String webUrl) {
        this.appId = appId;
        this.appType = appType;
        this.appName = appName;
        this.iconName = iconName;
        this.iconColor = iconColor;
        this.webUrl = webUrl;
    }

    public InsideApp(InsideApp app) {
        this.appId = app.appId;
        this.insideAppId = app.insideAppId;
        this.appType = app.appType;
        this.appName = app.appName;
        this.iconName = app.iconName;
        this.iconColor = app.iconColor;
        this.moduleName = app.moduleName;
        this.webUrl = app.webUrl;
    }
}

package vn.com.vng.zalopay.domain.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by khattn on 3/14/17.
 * Parse file config in resource app 1.
 */

public class InsideApp {

    public static class Constants {
        public static final long WITHDRAW = 1;
        public static final long RECHARGE = 2;
        public static final long LINK_CARD = 3;
        public static final long PAY_QR_CODE = 4;
        public static final long HISTORY = 5;
        public static final long SUPPORT_CENTER = 6;
        public static final long INFORMATION = 7;
        public static final long PROFILE = 8;
        public static final long NOTIFICATION = 9;
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

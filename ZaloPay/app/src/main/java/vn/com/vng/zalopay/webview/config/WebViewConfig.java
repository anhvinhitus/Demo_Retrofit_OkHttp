package vn.com.vng.zalopay.webview.config;

import vn.com.vng.zalopay.utils.RootUtils;

public class WebViewConfig {

    public static String getWebViewUrl(String host) {
        return host + "?muid=%s&maccesstoken=%s&appid=%d" + addParameterIsRoot();
    }

    public static String getResultWebViewUrl(String host) {
        return host + "/result/?apptransid=%s&muid=%s&maccesstoken=%s" + addParameterIsRoot();
    }

    public static String getHistoryWebViewUrl(String host) {
        return host + "/history/?muid=%s&maccesstoken=%s" + addParameterIsRoot();
    }

    private static String addParameterIsRoot() {
        return "&isroot=" + RootUtils.isDeviceRooted();
    }

    public static final String URL_TO_APP = "zalopay-1://backtoapp";

    public static final String URL_TO_LOGIN = "zalopay-1://backtologin";

    public static final String URL_PAY = "zalopay-1://post";

    public static final String URL_LOGIN_ZALO = "https://oauth.zaloapp.com/v2/auth";

    public static final String COOKIE_LOGIN_ZALO = "oauth.zaloapp.com";

    public static final String URL_LOGIN_FACEBOOK = "https://facebook.com";

    public static final String URL_LOGIN_FACEBOOK_MOBILE = "https://m.facebook.com";

    public static final String COOKIE_FACEBOOK = "facebook.com";

    public static final String COOKIE_FACEBOOK_MOBILE = "m.facebook.com";
}

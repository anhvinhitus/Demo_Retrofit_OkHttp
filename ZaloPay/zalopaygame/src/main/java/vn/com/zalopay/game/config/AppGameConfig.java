package vn.com.zalopay.game.config;

public class AppGameConfig {

    public static final String getWebViewUrl(String host) {
        return host + "?muid=%s&maccesstoken=%s&appid=%d";
    }

    public static final String getResultWebViewUrl(String host) {
        return host + "result/?apptransid=%s&muid=%s&maccesstoken=%s";
    }

    public static final String URL_TO_APP = "zalopay-1://backtoapp";

    public static final String URL_TO_LOGIN = "zalopay-1://backtologin";
}

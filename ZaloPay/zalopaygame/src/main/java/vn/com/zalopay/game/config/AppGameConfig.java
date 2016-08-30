package vn.com.zalopay.game.config;

import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;

public class AppGameConfig
{
    public static final String ZINGXU_PAGE      =   "";

    public static final String PAYGAME_PAGE     =   AppGameGlobal.getUrlConfig().getUrl() + "dichvu/?muid=%s&maccesstoken=%s&appid=%d";

    public static final String PAY_RESULT_PAGE  =   AppGameGlobal.getUrlConfig().getUrl() + "dichvu/result/?apptransid=%s&muid=%s&maccesstoken=%s";

    public static final String URL_TO_APP       =   "zalopay-1://backtoapp";

    public static final String URL_TO_LOGIN     =   "zalopay-1://backtologin";
}

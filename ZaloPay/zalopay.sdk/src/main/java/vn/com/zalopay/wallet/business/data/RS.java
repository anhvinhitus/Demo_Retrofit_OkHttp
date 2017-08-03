package vn.com.zalopay.wallet.business.data;

import android.support.annotation.StringRes;

import vn.com.zalopay.wallet.controller.SDKApplication;

public class RS {
    public static int getID(String pName) {
        return get(pName, "id");
    }

    @StringRes
    public static int getString(String pName) {
        return get(pName, "string");
    }

    private static int get(String pName, String pDef) {
        if (SDKApplication.getApplication() != null) {
            return SDKApplication.getApplication().getBaseContext().getResources()
                    .getIdentifier(pName, pDef, SDKApplication.getApplication().getPackageName());
        } else {
            return 0;
        }
    }

    public static final class string {

        public static final String sdk_website_callback_domain = "sdk_website_callback_domain";
        public static final String sdk_website3ds_callback_url = "sdk_website3ds_callback_url";
        public static final String sdk_website123pay_domain = "sdk_website123pay_domain";

        public static final String sdk_bidv_bankscript_term_of_use = "sdk_bidv_bankscript_term_of_use";
        public static final String sdk_bidv_bankscript_dknhdt = "sdk_bidv_bankscript_dknhdt";
        public static final String sdk_bidv_bankaccount_register_url = "sdk_bidv_bankaccount_register_url";

        public static final String app_service_id = "app_service_id";
        public static final String app_service_name = "app_service_name";

        public static final String sdk_banklogo_visa = "sdk_banklogo_visa";

        public static final String sdk_bidv_website_wrong_password_mess = "sdk_bidv_website_wrong_password_mess";
        public static final String sdk_bidv_website_wrong_captcha_mess = "sdk_bidv_website_wrong_captcha_mess";
        public static final String sdk_bidv_bankscript_auto_select_rule = "sdk_bidv_bankscript_auto_select_rule";

        public static final String allow_use_send_log_on_transactionfail = "allow_use_send_log_on_transactionfail";
        public static final String allow_use_fingerprint_feature = "allow_use_fingerprint_feature";
        public static final String allow_link_atm = "allow_link_atm";
        public static final String allow_link_cc = "allow_link_cc";
        public static final String allow_luhn_cc = "allow_luhn_cc";
        public static final String allow_luhn_atm = "allow_luhn_atm";
        public static final String allow_voucher = "allow_voucher";

        public static final String sdk_website_error_ssl_mess = "sdk_website_error_ssl_mess";
        public static final String sdk_website_errorcode_not_resolved_domain = "sdk_website_errorcode_not_resolved_domain";
        public static final String sdk_website_errorcode_timeout_load = "sdk_website_errorcode_timeout_load";
        public static final String sdk_website_errorcode_disconnected = "sdk_website_errorcode_disconnected";
        public static final String sdk_website_errorcode_timeout_connect = "sdk_website_errorcode_timeout_connect";

        public static final String sdk_website_errormess_not_resolved_domain = "sdk_website_errormess_not_resolved_domain";
        public static final String sdk_website_errormess_timeout_load = "sdk_website_errormess_timeout_load";

        public static final String sms_option = "sms_option";
        public static final String token_option = "token_option";

        public static final String sdk_font_regular = "sdk_font_regular";
        public static final String sdk_font_medium = "sdk_font_medium";
        public static final String sdk_font_unisec = "sdk_font_unisec";

        public static final String sdk_number_retry_otp = "sdk_number_retry_otp";
        public static final String sdk_retry_number_load_website = "sdk_retry_number_load_website";


        public static final String sdk_website_login_vcb_url = "sdk_website_login_vcb_url";
        public static final String sdk_vcb_otp_sms_type = "sdk_vcb_otp_sms_type";
        public static final String sdk_vcb_wallet_zalopay_type = "sdk_vcb_wallet_zalopay_type";
        public static final String sdk_vcb_number_retry_login = "sdk_vcb_number_retry_login";
        public static final String sdk_vcb_number_retry_password = "sdk_vcb_number_retry_password";
        public static final String sdk_vcb_number_retry_captcha = "sdk_vcb_number_retry_captcha";
        public static final String sdk_vcb_bankscript_generate_captcha = "sdk_vcb_bankscript_generate_captcha";
        public static final String sdk_vcb_bankscript_register_complete = "sdk_vcb_bankscript_register_complete";
        public static final String sdk_vcb_bankscript_unregister_complete = "sdk_vcb_bankscript_unregister_complete";
        public static final String sdk_vcb_flow_type = "sdk_vcb_flow_type";
        public static final String sdk_vcb_bankscript_auto_select_service = "sdk_vcb_bankscript_auto_select_service";
        public static final String prefix_numberphone_vcb = "prefix_numberphone_vcb";
        public static final String suffix_numberphone_vcb = "suffix_numberphone_vcb";
        public static final String sdk_vcb_invalid_captcha = "sdk_vcb_invalid_captcha";
    }

    public static final class layout {
        public static final String screen__vcb__login = "zpsdk_atm_vcb_login_page";
        public static final String screen__vcb__confirm_link = "zpsdk_atm_vcb_register_page";
        public static final String screen_vcb_otp = "zpsdk_atm_vcb_confirm_otp_page";
        public static final String screen__vcb__confirm_unlink = "zpsdk_atm_vcb_unregister_page";
        public static final String screen__linkacc__success = "screen__linkacc__success";
        public static final String screen__linkacc__fail = "screen__linkacc__fail";
        public static final String screen__unlinkacc__success = "screen__unlinkacc__success";
        public static final String screen__unlinkacc__fail = "screen__unlinkacc__fail";
        public static final String screen__link__acc = "screen__link__acc";

        public static final String screen__card = "screen__card";

        public static final String screen__success = "screen__success";
        public static final String screen__fail = "screen__fail";
        public static final String screen__fail_networking = "screen__fail_networking";
        public static final String screen__fail_processing = "screen__fail_processing";

        public static final String screen__zalopay__balance_error = "screen__zalopay__balance_error";
        public static final String screen__zalopay = "screen__zalopay";

        public static final String screen__local__card__authen = "screen__local__card__authen";
        public static final String screen__cover__bank__authen = "screen__cover__bank__authen";
    }

    public static final class drawable {
        public static final String ic_delete = "ic_del.png";
        public static final String ic_next = "ic_next.png";
        public static final String ic_bank_support_help = "ic_bank_support_help.png";
        public static final String ic_arrow = "ic_arrow.png";
        public static final String ic_round_delete = "ic_round_delete.png";
        public static final String ic_next_blue = "ic_next_blue.png";
        public static final String ic_next_blue_disable = "ic_next_blue_disable.png";
        public static final String ic_add_card = "ic_add_card.png";
    }
}

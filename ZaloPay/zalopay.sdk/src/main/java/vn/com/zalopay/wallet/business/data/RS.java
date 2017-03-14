package vn.com.zalopay.wallet.business.data;

import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.utils.Log;

public class RS {
    public static int getID(String pName) {
        return get(pName, "id");
    }

    public static int getString(String pName) {
        return get(pName, "string");
    }

    public static int getDrawable(String pName) {
        return get(pName, "drawable");
    }

    public static int getLayout(String pName) {
        return get(pName, "layout");
    }

    public static int getStyleable(String pName) {
        return get(pName, "styleable");
    }

    public static int getColor(String pName) {
        return get(pName, "color");
    }

    public static String getResourceName(int pResourceID) {
        if (SDKApplication.getInstance() != null) {
            return SDKApplication.getInstance().getResources().getResourceEntryName(pResourceID);
        }
        return null;
    }

    private static int get(String pName, String pDef) {
        if (SDKApplication.getInstance() != null) {
            int ret = SDKApplication.getInstance().getBaseContext().getResources()
                    .getIdentifier(pName, pDef, SDKApplication.getInstance().getPackageName());
            //Log.d("RS", pDef + pName + ret);
            return ret;
        } else {
            Log.e("RS:", pDef + ':' + pName + ":0");
            return 0;
        }
    }

    public static final class string {
        public static final String zpw_loading_website_message = "zpw_loading_website_message";
        public static final String zpw_alert_error_networking_when_load_banklist = "zpw_alert_error_networking_when_load_banklist";
        public static final String zpw_string_vcb_account_notfound_in_server = "zpw_string_vcb_account_notfound_in_server";
        public static final String zpw_string_vcb_account_in_server = "zpw_string_vcb_account_in_server";
        public static final String zpw_string_vcb_phonenumber_notfound_register = "zpw_string_vcb_phonenumber_notfound_register";
        public static final String zpw_string_vcb_phonenumber_notfound_unregister = "zpw_string_vcb_phonenumber_notfound_unregister";
        public static final String zpw_alert_networking_error_parse_website = "zpw_alert_networking_error_parse_website";

        public static final String zpw_warning_bidv_select_linkcard_payment = "zpw_warning_bidv_select_linkcard_payment";
        public static final String zpw_warning_bidv_linkcard_before_payment = "zpw_warning_bidv_linkcard_before_payment";

        public static final String zpw_error_message_bidv_website_wrong_password = "zpw_error_message_bidv_website_wrong_password";
        public static final String zpw_error_message_bidv_website_wrong_captcha = "zpw_error_message_bidv_website_wrong_captcha";

        public static final String zpw_channelname_vietcombank_mapaccount = "zpw_channelname_vietcombank_mapaccount";

        public static final String dialog_linkaccount_button = "dialog_linkaccount_button";
        public static final String dialog_retry_other_channel = "dialog_retry_other_channel";
        public static final String zpw_warning_vietcombank_linkcard_before_payment = "zpw_warning_vietcombank_linkcard_before_payment";
        public static final String zpw_warning_vietcombank_select_linkcard_payment = "zpw_warning_vietcombank_select_linkcard_payment";

        public static final String zpw_warning_vietcombank_linkbankaccount_not_linkcard = "zpw_warning_vietcombank_linkbankaccount_not_linkcard";

        public static final String dialog_retry_input_card_button = "dialog_retry_input_card_button";

        public static final String zpw_string_link_acc = "zpw_string_link_acc";
        public static final String zpw_string_unlink_acc = "zpw_string_unlink_acc";

        public static final String zpw_sdkreport_error_message = "zpw_sdkreport_error_message";

        public static final String allow_use_send_log_on_transactionfail = "allow_use_send_log_on_transactionfail";
        public static final String allow_use_tracking_timing = "allow_use_tracking_timing";
        public static final String allow_use_fingerprint_feature = "allow_use_fingerprint_feature";
        public static final String zpw_error_authen_pin = "zpw_error_authen_pin";

        public static final String zpw_fail_transanction_by_ssl = "zpw_fail_transanction_by_ssl";
        public static final String zpw_string_error_code_name_not_resolved = "zpw_string_error_code_name_not_resolved";
        public static final String zpw_string_error_code_timeout = "zpw_string_error_code_timeout";
        public static final String zpw_string_error_code_disconnected = "zpw_string_error_code_disconnected";

        public static final String zpw_string_error_friendlymessage_name_not_resolved = "zpw_string_error_friendlymessage_name_not_resolved";
        public static final String zpw_string_error_friendlymessage_timeout = "zpw_string_error_friendlymessage_timeout";
        public static final String zpw_string_error_friendlymessage_end_transaction = "zpw_string_error_friendlymessage_end_transaction";

        public static final String time_to_live_transaction = "time_to_live_transaction";
        public static final String zpw_string_transaction_expired = "zpw_string_transaction_expired";
        public static final String zpw_alert_cardname_has_whitespace = "zpw_alert_cardname_has_whitespace";
        public static final String zpw_string_pin_wrong = "zpw_string_pin_wrong";
        public static final String zpw_link_card_existed = "zpw_link_card_existed";

        public static final String cardview_rate = "cardview_rate";
        public static final String percent_ontablet = "percent_ontablet";
        public static final String percent_ondefault = "percent_ondefault";

        public static final String sms_option = "sms_option";
        public static final String token_option = "token_option";

        public static final String zpw_missing_card_number = "zpw_missing_card_number";

        public static final String zpw_string_alert_networking_not_stable = "zpw_string_alert_networking_not_stable";
        public static final String zpw_string_alert_networking_offline = "zpw_string_alert_networking_offline";
        public static final String zpw_string_alert_networking_online = "zpw_string_alert_networking_online";
        public static final String zpw_string_remind_turn_on_networking = "zpw_string_remind_turn_on_networking";

        public static final String zpw_string_visible_pin_on = "zpw_string_visible_pin_on";
        public static final String zpw_string_visible_pin_off = "zpw_string_visible_pin_off";

        public static final String zpw_string_withdraw_description = "zpw_string_withdraw_description";

        public static final String zpw_string_level_allow_mapcard = "zpw_string_level_allow_mapcard";
        public static final String zpw_string_level_allow_withdraw = "zpw_string_level_allow_withdraw";
        public static final String zpw_string_level_allow_use_zalopay = "zpw_string_level_allow_use_zalopay";
        public static final String zpw_string_level_allow_bankaccount = "zpw_string_level_allow_bankaccount";

        public static final String zpw_redpackage_app_id = "zpw_redpackage_app_id";
        public static final String zpw_string_pay_domain = "zpw_string_pay_domain";

        public static final String zpw_string_lixi_notice_title_02 = "zpw_string_lixi_notice_title_02";
        public static final String zpw_string_lixi_notice_title = "zpw_string_lixi_notice_title";

        public static final String zpw_string_linkacc_notice_description = "zpw_string_linkacc_notice_description";
        public static final String zpw_string_unlinkacc_notice_description = "zpw_string_unlinkacc_notice_description";

        public static final String zpw_string_error_storage = "zpw_string_error_storage";

        public static final String zpw_string_error_parse_web = "zpw_string_error_parse_web";

        public static final String zpw_alert_error_resource_not_download = "zpw_alert_error_resource_not_download";

        public static final String walletsdk_string_bar_title = "walletsdk_string_bar_title";

        public static final String zpw_string_pay_title = "zpw_string_pay_title";

        public static final String zpw_string_tranfer_title = "zpw_string_tranfer_title";

        public static final String zpw_string_withdraw_title = "zpw_string_withdraw_title";

        public static final String zpw_string_alert_loading_bank = "zpw_string_alert_loading_bank";

        public static final String zpw_string_fee_label = "zpw_string_fee_label";

        public static final String zpw_string_vnd = "zpw_string_vnd";

        public static final String zpw_string_title_select_card = "zpw_string_title_select_card";

        public static final String zpw_string_title_payment_gateway = "zpw_string_title_payment_gateway";
        public static final String zpw_string_title_payment_gateway_tranfer = "zpw_string_title_payment_gateway_tranfer";
        public static final String zpw_string_title_payment_gateway_topup = "zpw_string_title_payment_gateway_topup";
        public static final String zpw_string_title_payment_gateway_withdraw = "zpw_string_title_payment_gateway_withdraw";

        public static final String zpw_string_title_payment_gateway_confirm_pay = "zpw_string_title_payment_gateway_confirm_pay";
        public static final String zpw_string_title_payment_gateway_confirm_topup = "zpw_string_title_payment_gateway_confirm_topup";
        public static final String zpw_string_title_payment_gateway_confirm_tranfer = "zpw_string_title_payment_gateway_confirm_tranfer";
        public static final String zpw_string_title_payment_gateway_confirm_withdraw = "zpw_string_title_payment_gateway_confirm_withdraw";

        public static final String zpw_string_merchant_creditcard_3ds_url_prefix = "zpw_string_merchant_creditcard_3ds_url_prefix";
        public static final String zpw_font_regular = "zpw_font_regular";
        public static final String zpw_font_medium = "zpw_font_medium";
        public static final String zpw_font_unisec = "zpw_font_unisec";

        public static final String zingpaysdk_confirm_quit = "zingpaysdk_confirm_quit";
        public static final String zingpaysdk_confirm_quit_rescan_qrcode = "zingpaysdk_confirm_quit_rescan_qrcode";

        public static final String zpw_confirm_quit_loadsite = "zpw_confirm_quit_loadsite";

        public static final String zpw_string_error_layout = "zpw_string_error_layout";
        public static final String zpw_string_error_payment_info = "zpw_string_error_payment_info";

        public static final String zpw_string_not_need_save_pin = "zpw_string_not_need_save_pin";
        public static final String zpw_error_transaction_duplicate_message = "zpw_error_transaction_duplicate_message";

        public static final String zpw_error_paymentinfo = "zpw_error_paymentinfo";

        public static final String zpw_not_allow_payment_app = "zpw_not_allow_payment_app";

        public static final String zpw_alert_captcha_vietcombank = "zpw_alert_captcha_vietcombank";
        public static final String zpw_alert_captcha_vietcombank_update = "zpw_alert_captcha_vietcombank_update";

        public static final String dialog_continue_load_button = "dialog_continue_load_button";
        public static final String dialog_cancel_button = "dialog_cancel_button";
        public static final String dialog_continue_button = "dialog_continue_button";
        public static final String dialog_getstatus_button = "dialog_getstatus_button";
        public static final String dialog_quit_button = "dialog_quit_button";
        public static final String dialog_close_button = "dialog_close_button";
        public static final String dialog_retry_button = "dialog_retry_button";
        public static final String dialog_later_button = "dialog_later_button";
        public static final String dialog_choose_again_button = "dialog_choose_again_button";
        public static final String dialog_upgrade_button = "dialog_upgrade_button";
        public static final String dialog_comeback_button = "dialog_comeback_button";
        public static final String dialog_agree_button = "dialog_agree_button";
        public static final String dialog_linkcard_button = "dialog_linkcard_button";

        public static final String dialog_co_button = "dialog_co_button";
        public static final String dialog_khong_button = "dialog_khong_button";

        public static final String zpw_string_alert_min_amount_input = "zpw_string_alert_min_amount_input";
        public static final String zpw_string_alert_max_amount_input = "zpw_string_alert_max_amount_input";

        public static final String zingpaysdk_alert_process_view = "zingpaysdk_alert_process_view";

        public static final String zingpaysdk_alert_transition_screen = "zingpaysdk_alert_transition_screen";

        public static final String zpw_save_credit_card = "zpw_save_credit_card";
        public static final String zpw_save_credit_card_atm = "zpw_save_credit_card_atm";
        public static final String zpw_save_credit_card_default = "zpw_save_credit_card_default";

        public static final String zpw_string_bank_maintenance = "zpw_string_bank_maintenance";
        public static final String zpw_string_channel_maintenance = "zpw_string_channel_maintenance";

        public static final String zpw_string_get_card_info_processing = "zpw_string_get_card_info_processing";

        public static final String zingpaysdk_alert_processing_check_app_info = "zingpaysdk_alert_processing_check_app_info";
        public static final String zingpaysdk_alert_processing_loading_resource = "zingpaysdk_alert_processing_loading_resource";

        public static final String zingpaysdk_alert_network_error = "zingpaysdk_alert_network_error";
        public static final String zpw_alert_networking_off_in_transaction = "zpw_alert_networking_off_in_transaction";

        public static final String zpw_alert_network_error_getappinfo = "zpw_alert_network_error_getappinfo";
        public static final String zingpaysdk_missing_app_user = "zingpaysdk_missing_app_user";

        public static final String zpw_alert_networking_error_check_status = "zpw_alert_networking_error_check_status";
        public static final String zpw_alert_process_error = "zpw_alert_process_error";

        public static final String zingpaysdk_alert_no_connection = "zingpaysdk_alert_no_connection";
        public static final String zingpaysdk_alert_processing = "zingpaysdk_alert_processing";
        public static final String zingpaysdk_alert_checking = "zingpaysdk_alert_checking";
        public static final String zpw_string_alert_submit_order = "zpw_string_alert_submit_order";
        public static final String zingpaysdk_alert_get_status = "zingpaysdk_alert_get_status";

        public static final String zpw_alert_order_not_submit = "zpw_alert_order_not_submit";

        public static final String zpw_alert_empty_creditcard_url = "zpw_alert_empty_creditcard_url";

        public static final String zingpaysdk_alert_processing_ask_to_retry = "zingpaysdk_alert_processing_ask_to_retry";
        public static final String zingpaysdk_alert_error_networking_ask_to_retry = "zingpaysdk_alert_error_networking_ask_to_retry";

        public static final String zingpaysdk_alert_processing_get_status_fail = "zingpaysdk_alert_processing_get_status_fail";
        public static final String zingpaysdk_alert_processing_get_status_linkcard_fail = "zingpaysdk_alert_processing_get_status_linkcard_fail";
        public static final String zingpaysdk_alert_transaction_success = "zingpaysdk_alert_transaction_success";

        public static final String zpw_string_load_website_timeout_message = "zpw_string_load_website_timeout_message";

        public static final String zpw_string_bank_maitenance = "zpw_string_bank_maitenance";

        public static final String zpw_string_bank_not_support = "zpw_string_bank_not_support";

        public static final String zpw_string_authen_atm = "zpw_string_authen_atm";

        public static final String zingpaysdk_alert_network_error_download_resource = "zingpaysdk_alert_network_error_download_resource";


        public static final String zingpaysdk_conf_gwinfo_channel_link_card = "zingpaysdk_conf_gwinfo_channel_link_card";
        public static final String zingpaysdk_conf_gwinfo_channel_zalopay = "zingpaysdk_conf_gwinfo_channel_zalopay";
        public static final String zingpaysdk_conf_gwinfo_channel_atm = "zingpaysdk_conf_gwinfo_channel_atm";
        public static final String zingpaysdk_conf_gwinfo_channel_credit_card = "zingpaysdk_conf_gwinfo_channel_credit_card";
        public static final String zingpaysdk_conf_gwinfo_channel_wallet_transfer = "zingpaysdk_conf_gwinfo_channel_wallet_transfer";
        public static final String zingpaysdk_conf_gwinfo_channel_withdraw = "zingpaysdk_conf_gwinfo_channel_withdraw";
        public static final String zingpaysdk_conf_gwinfo_channel_link_acc = "zingpaysdk_conf_gwinfo_channel_link_acc";
        public static final String zingpaysdk_conf_gwinfo_channel_bankaccount = "zingpaysdk_conf_gwinfo_channel_bankaccount";

        public static final String zpw_conf_wallet_linkcard_desc = "zpw_conf_wallet_linkcard_desc";
        public static final String zpw_conf_wallet_id = "zpw_conf_wallet_id";
        public static final String zpw_conf_wallet_amount = "zpw_conf_wallet_amount";

        public static final String zingpaysdk_intent_key_channel = "zingpaysdk_intent_key_channel";
        public static final String zingpaysdk_invalid_app_amount = "zingpaysdk_invalid_app_amount";
        public static final String zingpaysdk_missing_mac_info = "zingpaysdk_missing_mac_info";
        public static final String zalopay_invalid_app_id = "zalopay_invalid_app_id";
        public static final String zingpaysdk_invalid_user_id_user_name = "zingpaysdk_invalid_user_id_user_name";
        public static final String zingpaysdk_invalid_app_time = "zingpaysdk_invalid_app_time";
        public static final String zingpaysdk_invalid_app_trans = "zingpaysdk_invalid_app_trans";
        public static final String zingpaysdk_missing_app_pmt_info = "zingpaysdk_missing_app_pmt_info";
        public static final String zingpaysdk_invalid_token = "zingpaysdk_invalid_token";

        public static final String zpw_string_fee_free = "zpw_string_fee_free";

        public static final String zpw_string_channel_not_allow = "zpw_string_channel_not_allow";
        public static final String zpw_string_channel_not_allow_by_amount = "zpw_string_channel_not_allow_by_amount";
        public static final String zpw_string_channel_not_allow_by_amount_small = "zpw_string_channel_not_allow_by_amount_small";
        public static final String zpw_string_channel_not_allow_by_fee = "zpw_string_channel_not_allow_by_fee";

        public static final String zpw_string_card_error_luhn = "zpw_string_card_error_luhn";
        public static final String zpsdk_luhn_check_cc = "zpsdk_luhn_check_cc";
        public static final String zpsdk_luhn_check_atm = "zpsdk_luhn_check_atm";
        public static final String zpw_alert_cardname_wrong = "zpw_alert_cardname_wrong";

        public static final String zingpaysdk_pmc_name_zalopay = "zingpaysdk_pmc_name_zalopay";
        public static final String zpw_string_zalopay_balance_error_label = "zpw_string_zalopay_balance_error_label";
        public static final String zpw_string_zalopay_balance_label = "zpw_string_zalopay_balance_label";

        public static final String zpw_string_atm_method_name = "zpw_string_atm_method_name";
        public static final String zpw_string_credit_card_method_name = "zpw_string_credit_card_method_name";
        public static final String zpw_string_credit_card_link = "zpw_string_credit_card_link";
        public static final String zpw_string_zalopay_wallet_method_name = "zpw_string_zalopay_wallet_method_name";

        public static final String zpw_string_card_number_hint = "zpw_string_card_number_hint";
        public static final String zpw_string_card_name_hint = "zpw_string_card_name_hint";

        public static final String zpw_string_card_not_support = "zpw_string_card_not_support";

        public static final String zpw_string_payment_currency_label = "zpw_string_payment_currency_label";

        public static final String zpw_string_credit_card_save_label = "zpw_string_credit_card_save_label";


        public static final String zpw_string_hidden_character = "zpw_string_hidden_character";
        public static final String zpw_string_title_header_pay_result = "zpw_string_title_header_pay_result";

        public static final String zpw_string_not_enough_money_wallet = "zpw_string_not_enough_money_wallet";

        public static final String zpw_app_info_exclude_channel = "zpw_app_info_exclude_channel";
        public static final String zpw_string_alert_linkcard_channel_withdraw = "zpw_string_alert_linkcard_channel_withdraw";

        public static final String zingpaysdk_alert_input_error = "zingpaysdk_alert_input_error";

        public static final String zpw_alert_error_data = "zpw_alert_error_data";

        public static final String zpw_string_alert_remove_mapcard_error_networking = "zpw_string_alert_remove_mapcard_error_networking";

        public static final String zpw_string_title_select_bank = "zpw_string_title_select_bank";

        public static final String zpw_button_submit_text = "zpw_button_submit_text";

        public static final String zpw_string_save_card_error = "zpw_string_save_card_error";
        public static final String zpw_string_get_bank_account_error = "zpw_string_get_bank_account_error";

        public static final String zpw_string_number_retry = "zpw_string_number_retry";
        public static final String zpw_string_alert_over_retry_otp = "zpw_string_alert_over_retry_otp";
        public static final String zpw_string_number_load_web_retry = "zpw_string_number_load_web_retry";

        public static final String zpw_string_error_system = "zpw_string_error_system";

        public static final String zingpaysdk_alert_processing_bank = "zingpaysdk_alert_processing_bank";

        public static final String zingpaysdk_alert_processing_otp = "zingpaysdk_alert_processing_otp";

        public static final String zpw_string_confirm_upgrade_level_save_card = "zpw_string_confirm_upgrade_level_save_card";
        /**
         * USER PROFILE
         */
        public static final String zpw_string_alert_profilelevel_update = "zpw_string_alert_profilelevel_update";
        public static final String zpw_string_fee_upgrade_level = "zpw_string_fee_upgrade_level";
        public static final String zpw_string_alert_profilelevel_update_and_before_payby_bankaccount = "zpw_string_alert_profilelevel_update_and_before_payby_bankaccount";
        public static final String zpw_string_alert_profilelevel_update_and_linkaccount_before_payment = "zpw_string_alert_profilelevel_update_and_linkaccount_before_payment";
        /***
         * LINK CARD.
         */
        public static final String zpw_string_exit_without_pin = "zpw_string_exit_without_pin";
        public static final String zpw_string_title_require_pin_page = "zpw_string_title_require_pin_page";
        public static final String zpw_string_alert_userinfo_invalid = "zpw_string_alert_userinfo_invalid";

        /***
         * payment result.
         */
        public static final String zpw_string_payment_success_label = "zpw_string_payment_success_label";
        public static final String zpw_string_tranfer_success_label = "zpw_string_tranfer_success_label";
        public static final String zpw_string_topup_success_label = "zpw_string_topup_success_label";
        public static final String zpw_string_linkcard_success_label = "zpw_string_linkcard_success_label";
        public static final String zpw_string_lixi_success_label = "zpw_string_lixi_success_label";
        public static final String zpw_string_withdraw_success_label = "zpw_string_withdraw_success_label";
        public static final String zpw_string_alert_app_user_invalid_tranfer = "zpw_string_alert_app_user_invalid_tranfer";
        public static final String zpw_string_alert_app_user_invalid_linkcard = "zpw_string_alert_app_user_invalid_linkcard";

        public static final String zpw_string_payment_fail_transaction = "zpw_string_payment_fail_transaction";
        public static final String zpw_string_payment_fail_linkcard = "zpw_string_payment_fail_linkcard";

        public static final String zpw_string_transaction_processing = "zpw_string_transaction_processing";
        public static final String zpw_string_linkcard_processing = "zpw_string_linkcard_processing";
        public static final String zpw_string_transaction_networking_error = "zpw_string_transaction_networking_error";

        public static final String zpw_string_alert_card_not_found = "zpw_string_alert_card_not_found";
        public static final String zpw_string_alert_card_save_error = "zpw_string_alert_card_save_error";
        public static final String zpw_string_alert_maintenance = "zpw_string_alert_maintenance";

        public static final String dialog_turn_on = "dialog_turn_on";
        public static final String dialog_turn_off = "dialog_turn_off";

        public static final String zpw_string_bankcode_viettinbank = "zpw_string_bankcode_viettinbank";
        public static final String zpw_string_bankcode_sacombank = "zpw_string_bankcode_sacombank";
        public static final String zpw_string_bankcode_commercialbank = "zpw_string_bankcode_commercialbank";
        public static final String zpw_string_bankcode_vietcombank = "zpw_string_bankcode_vietcombank";
        public static final String zpw_string_bankcode_bidv = "zpw_string_bankcode_bidv";

        public static final String zpw_string_bankcode_visa = "zpw_string_bankcode_visa";
        public static final String zpw_string_bankcode_master = "zpw_string_bankcode_master";
        public static final String zpw_string_bankcode_jcb = "zpw_string_bankcode_jcb";

        public static final String zpw_alert_linkcard_not_support = "zpw_alert_linkcard_not_support";

        public static final String zpw_string_vcb_link_login = "zpw_string_vcb_link_login";
        public static final String zpw_vcb_value_otp_sms = "zpw_vcb_value_otp_sms";
        public static final String zpw_vcb_wallet_type = "zpw_vcb_wallet_type";

        public static final String zpw_string_title_err_login_vcb = "zpw_string_title_err_login_vcb";

        public static final String zpw_string_vcb_empty_username = "zpw_string_vcb_empty_username";
        public static final String zpw_string_vcb_empty_password = "zpw_string_vcb_empty_password";
        public static final String zpw_string_vcb_empty_captcha_login = "zpw_string_vcb_empty_captcha_login";
        public static final String zpw_string_vcb_empty_captcha_confirm = "zpw_string_vcb_empty_captcha_confirm";
        public static final String zpw_string_vcb_wrong_username_password = "zpw_string_vcb_wrong_username_password";
        public static final String zpw_string_vcb_wrong_captcha = "zpw_string_vcb_wrong_captcha";
        public static final String zpw_string_vcb_account_locked = "zpw_string_vcb_account_locked";
        public static final String zpw_string_vcb_wrong_times_allow = "zpw_string_vcb_wrong_times_allow";
        public static final String zpw_int_vcb_num_times_allow_login_wrong = "zpw_int_vcb_num_times_allow_login_wrong";

        // auto-fill otp
        public static final String zpw_string_vcb_otp_sender = "zpw_string_vcb_otp_sender";
        public static final String zpw_int_vcb_otp_size = "zpw_int_vcb_otp_size";
        public static final String zpw_string_vcb_otp_identify = "zpw_string_vcb_otp_identify";
        public static final String zpw_string_vcb_otp_prefixOtp = "zpw_string_vcb_otp_prefixOtp";

        public static final String zpw_string_linkacc_captcha_hint = "zpw_string_linkacc_captcha_hint";

        public static final String zpw_string_special_bankscript_vcb_generate_captcha = "zpw_string_special_bankscript_vcb_generate_captcha";
        public static final String zpw_string_special_bankscript_vcb_register_complete = "zpw_string_special_bankscript_vcb_register_complete";
        public static final String zpw_string_special_bankscript_vcb_unregister_complete = "zpw_string_special_bankscript_vcb_unregister_complete";
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

        public static final String screen__success__special = "screen__success__special";

        public static final String screen__require_pin = "screen__require_pin";

        public static final String screen__fail = "screen__fail";

        public static final String screen__fail_networking = "screen__fail_networking";

        public static final String screen__fail_processing = "screen__fail_processing";

        public static final String screen__zalopay__balance_error = "screen__zalopay__balance_error";

        public static final String screen__gateway = "screen__gateway";

        public static final String screen__zalopay = "screen__zalopay";

        public static final String listview__item__channel__gateway = "listview__item__channel__gateway";

        public static final String screen__local__card__authen = "screen__local__card__authen";

        public static final String screen__cover__bank__authen = "screen__cover__bank__authen";

        public static final String screen_selection_account_list = "zpsdk_atm_vcb_account_list";

        public static final String screen__confirm = "screen__confirm";

    }

    public static final class drawable {
        public static final String zpw_bg_button = "zpw_bg_button";
        public static final String zpw_bg_button_disable = "zpw_bg_button_disable";
        public static final String zpw_bg_button_final = "zpw_bg_button_final";
        public static final String ic_info = "ic_info.png";
        public static final String ic_delete = "ic_del.png";

        public static final String ic_checked = "ic_checked.png";

        public static final String ic_next = "ic_next.png";

        public static final String ic_question = "ic_question.png";

        public static final String ic_bank_support_help = "ic_bank_support_help.png";

    }

    public static final class styleable {
    }

    public static final class color {
    }
}

package vn.com.vng.zalopay.data;

import android.content.Context;

public class NetworkError {

    public static final int SUCCESSFUL = 1;
    public static final int EXCEPTION = 0;
    public static final int ZK_NODE_EXIST_EXCEPTION = -1;
    public static final int APPID_INVALID = -2;
    public static final int APP_NOT_AVAILABLE = -3;
    public static final int APP_TIME_INVALID = -4;
    public static final int AMOUNT_INVALID = -5;
    public static final int PLATFORM_INVALID = -6;
    public static final int PLATFORM_NOT_AVAILABLE = -7;
    public static final int DSCREEN_TYPE_INVALID = -8;
    public static final int PMCID_INVALID = -9;
    public static final int PMC_INACTIVE = -10;
    public static final int APPTRANSID_EXIST = -70;
    public static final int DUPLICATE_ZPTRANSID = -69;
    public static final int GET_TRANSID_FAIL = -13;
    public static final int SET_CACHE_FAIL = -14;
    public static final int GET_CACHE_FAIL = -15;
    public static final int UPDATE_RESULT_FAIL = -16;
    public static final int EXCEED_MAX_NOTIFY = -17;
    public static final int DEVICEID_NOT_MATCH = -18;
    public static final int APPID_NOT_MATCH = -19;
    public static final int PLATFORM_NOT_MATCH = -20;
    public static final int PMC_FACTORY_NOT_FOUND = -21;
    public static final int ZALO_LOGIN_FAIL = -71;
    public static final int ZALO_LOGIN_EXPIRE = -72;
    public static final int TOKEN_INVALID = -73;
    public static final int CARDINFO_INVALID = -74;
    public static final int CARDINFO_EXIST = -75;
    public static final int SDK_INVALID = -26;
    public static final int CARDINFO_NOT_FOUND = -76;
    public static final int UM_TOKEN_NOT_FOUND = -77;
    public static final int ATM_CREATE_ORDER_DBG_FAIL = -29;
    public static final int UM_TOKEN_EXPIRE = -78;
    public static final int REQUEST_FORMAT_INVALID = -79;
    public static final int CARD_INVALID = -31;
    public static final int APP_INACTIVE = -32;
    public static final int APP_MAINTENANCE = -33;
    public static final int PMC_MAINTENANCE = -34;
    public static final int PMC_NOT_AVAILABLE = -35;
    public static final int OVER_LIMIT = -36;
    public static final int DUPLICATE = 2;
    public static final int CREATE_ORDER_SUCCESSFUL = 3;
    public static final int IN_NOTIFY_QUEUE = 4;
    public static final int PROCESSING = 5;
    public static final int TRANS_NOT_FINISH = -80;
    public static final int ATM_WAIT_FOR_CHARGE = 9;
    public static final int INIT = 10;
    public static final int USER_NOT_MATCH = -81;
    public static final int NOT_FOUND_SMS_SERVICE_PHONE = -39;
    public static final int MAX_RETRY_GET_DBG_STATUS = -40;
    public static final int ATM_CREATE_ORDER_FAIL = -41;
    public static final int ATM_BANK_INVALID = -42;
    public static final int ATM_BANK_MAINTENANCE = -43;
    public static final int DUPLICATE_APPTRANSID = -68;
    public static final int ATM_VERIFY_CARD_SUCCESSFUL = 12;
    public static final int ATM_VERIFY_OTP_SUCCESS = 13;
    public static final int ATM_VERIFY_CARD_FAIL = -44;
    public static final int ATM_MAX_RETRY_OTP_FAIL = -45;
    public static final int ATM_QUERY_ORDER_FAIL = -46;
    public static final int ATM_BANK_SRC_INVALID = -47;
    public static final int DESERIALIZE_TRANS_FAIL = -67;
    public static final int IN_GET_STATUS_ATM_QUEUE = 15;
    public static final int ATM_CHARGE_FAIL = -48;
    public static final int ATM_RETRY_CAPTCHA = 16;
    public static final int ATM_RETRY_OTP = 17;
    public static final int ATM_CHARGE_SUCCESSFUL = 18;
    public static final int TRANS_INFO_NOT_FOUND = -49;
    public static final int ATM_CAPTCHA_INVALID = -50;
    public static final int ATM_COST_RATE_INVALID = -51;
    public static final int ITEMS_INVALID = -52;
    public static final int HMAC_INVALID = -53;
    public static final int TIME_INVALID = -54;
    public static final int CAL_NET_CHARGE_AMT_FAIL = -55;
    public static final int ATM_VERIFY_OTP_FAIL = -56;
    public static final int APP_USER_INVALID = -57;
    public static final int ZPW_GETTRANSID_FAIL = -58;
    public static final int ZPW_PURCHASE_FAIL = -59;
    public static final int ZPW_ACCOUNT_NAME_INVALID = -60;
    public static final int ZPW_ACCOUNT_SUSPENDED = -61;
    public static final int ZPW_ACCOUNT_NOT_EXIST = -62;
    public static final int ZPW_BALANCE_NOT_ENOUGH = -63;
    public static final int ZPW_GET_BALANCE_FAIL = -64;
    public static final int ZPW_WRONG_PASSWORD = -65;
    public static final int USER_INVALID = -66;
    public static final int CARD_NOT_MATCH = -82;
    public static final int TRANSID_FORMAT_INVALID = -83;
    public static final int CARD_TOKEN_INVALID = -84;
    public static final int CARD_TOKEN_EXPIRE = -85;
    public static final int TRANSTYPE_INVALID = -86;
    public static final int TRANSTYPE_INACTIVE = -87;
    public static final int TRANSTYPE_MAINTENANCE = -88;
    public static final int APPTRANSID_GEN_ERROR = -93;
    public static final int MAP_APPID_APPTRANSID_FAIL = -89;
    public static final int EXCEED_MAX_NOTIFY_WALLET_FEE = -90;
    public static final int UPDATE_RESULT_FAIL_WALLET_FEE = -91;
    public static final int APPTRANSID_INVALID = -92;
    public static final int TRANSTYPE_AMOUNT_INVALID = -94;
    public static final int CARD_ALREADY_MAP = -95;
    public static final int SERVER_MAINTAIN = -999;
    public static final int INVITATION_CODE_ERROR = 24;


    public static String create(Context context, int errorCode) {
        switch (errorCode) {
            case SUCCESSFUL:
                return context.getString(R.string.exception_successful);
            case EXCEPTION:
                return context.getString(R.string.exception_exception);
            case ZK_NODE_EXIST_EXCEPTION:
                return context.getString(R.string.exception_zk_node_exist_exception);
            case APPID_INVALID:
                return context.getString(R.string.exception_appid_invalid);
            case APP_NOT_AVAILABLE:
                return context.getString(R.string.exception_app_not_available);
            case APP_TIME_INVALID:
                return context.getString(R.string.exception_app_time_invalid);
            case AMOUNT_INVALID:
                return context.getString(R.string.exception_amount_invalid);
            case PLATFORM_INVALID:
                return context.getString(R.string.exception_platform_invalid);
            case PLATFORM_NOT_AVAILABLE:
                return context.getString(R.string.exception_platform_not_available);
            case DSCREEN_TYPE_INVALID:
                return context.getString(R.string.exception_dscreen_type_invalid);
            case PMCID_INVALID:
                return context.getString(R.string.exception_pmcid_invalid);
            case PMC_INACTIVE:
                return context.getString(R.string.exception_pmc_inactive);
            case APPTRANSID_EXIST:
                return context.getString(R.string.exception_apptransid_exist);
            case DUPLICATE_ZPTRANSID:
                return context.getString(R.string.exception_duplicate_zptransid);
            case GET_TRANSID_FAIL:
                return context.getString(R.string.exception_get_transid_fail);
            case SET_CACHE_FAIL:
                return context.getString(R.string.exception_set_cache_fail);
            case GET_CACHE_FAIL:
                return context.getString(R.string.exception_get_cache_fail);
            case UPDATE_RESULT_FAIL:
                return context.getString(R.string.exception_update_result_fail);
            case EXCEED_MAX_NOTIFY:
                return context.getString(R.string.exception_exceed_max_notify);
            case DEVICEID_NOT_MATCH:
                return context.getString(R.string.exception_deviceid_not_match);
            case APPID_NOT_MATCH:
                return context.getString(R.string.exception_appid_not_match);
            case PLATFORM_NOT_MATCH:
                return context.getString(R.string.exception_platform_not_match);
            case PMC_FACTORY_NOT_FOUND:
                return context.getString(R.string.exception_pmc_factory_not_found);
            case ZALO_LOGIN_FAIL:
                return context.getString(R.string.exception_zalo_login_fail);
            case ZALO_LOGIN_EXPIRE:
                return context.getString(R.string.exception_zalo_login_expire);
            case TOKEN_INVALID:
                return context.getString(R.string.exception_token_invalid);
            case CARDINFO_INVALID:
                return context.getString(R.string.exception_cardinfo_invalid);
            case CARDINFO_EXIST:
                return context.getString(R.string.exception_cardinfo_exist);
            case SDK_INVALID:
                return context.getString(R.string.exception_sdk_invalid);
            case CARDINFO_NOT_FOUND:
                return context.getString(R.string.exception_cardinfo_not_found);
            case UM_TOKEN_NOT_FOUND:
                return context.getString(R.string.exception_um_token_not_found);
            case ATM_CREATE_ORDER_DBG_FAIL:
                return context.getString(R.string.exception_atm_create_order_dbg_fail);
            case UM_TOKEN_EXPIRE:
                return context.getString(R.string.exception_um_token_expire);
            case REQUEST_FORMAT_INVALID:
                return context.getString(R.string.exception_request_format_invalid);
            case CARD_INVALID:
                return context.getString(R.string.exception_card_invalid);
            case APP_INACTIVE:
                return context.getString(R.string.exception_app_inactive);
            case APP_MAINTENANCE:
                return context.getString(R.string.exception_app_maintenance);
            case PMC_MAINTENANCE:
                return context.getString(R.string.exception_pmc_maintenance);
            case PMC_NOT_AVAILABLE:
                return context.getString(R.string.exception_pmc_not_available);
            case OVER_LIMIT:
                return context.getString(R.string.exception_over_limit);
            case DUPLICATE:
                return context.getString(R.string.exception_duplicate);
            case CREATE_ORDER_SUCCESSFUL:
                return context.getString(R.string.exception_create_order_successful);
            case IN_NOTIFY_QUEUE:
                return context.getString(R.string.exception_in_notify_queue);
            case PROCESSING:
                return context.getString(R.string.exception_processing);
            case TRANS_NOT_FINISH:
                return context.getString(R.string.exception_trans_not_finish);
            case ATM_WAIT_FOR_CHARGE:
                return context.getString(R.string.exception_atm_wait_for_charge);
            case INIT:
                return context.getString(R.string.exception_init);
            case USER_NOT_MATCH:
                return context.getString(R.string.exception_user_not_match);
            case NOT_FOUND_SMS_SERVICE_PHONE:
                return context.getString(R.string.exception_not_found_sms_service_phone);
            case MAX_RETRY_GET_DBG_STATUS:
                return context.getString(R.string.exception_max_retry_get_dbg_status);
            case ATM_CREATE_ORDER_FAIL:
                return context.getString(R.string.exception_atm_create_order_fail);
            case ATM_BANK_INVALID:
                return context.getString(R.string.exception_atm_bank_invalid);
            case ATM_BANK_MAINTENANCE:
                return context.getString(R.string.exception_atm_bank_maintenance);
            case DUPLICATE_APPTRANSID:
                return context.getString(R.string.exception_duplicate_apptransid);
            case ATM_VERIFY_CARD_SUCCESSFUL:
                return context.getString(R.string.exception_atm_verify_card_successful);
            case ATM_VERIFY_OTP_SUCCESS:
                return context.getString(R.string.exception_atm_verify_otp_success);
            case ATM_VERIFY_CARD_FAIL:
                return context.getString(R.string.exception_atm_verify_card_fail);
            case ATM_MAX_RETRY_OTP_FAIL:
                return context.getString(R.string.exception_atm_max_retry_otp_fail);
            case ATM_QUERY_ORDER_FAIL:
                return context.getString(R.string.exception_atm_query_order_fail);
            case ATM_BANK_SRC_INVALID:
                return context.getString(R.string.exception_atm_bank_src_invalid);
            case DESERIALIZE_TRANS_FAIL:
                return context.getString(R.string.exception_deserialize_trans_fail);
            case IN_GET_STATUS_ATM_QUEUE:
                return context.getString(R.string.exception_in_get_status_atm_queue);
            case ATM_CHARGE_FAIL:
                return context.getString(R.string.exception_atm_charge_fail);
            case ATM_RETRY_CAPTCHA:
                return context.getString(R.string.exception_atm_retry_captcha);
            case ATM_RETRY_OTP:
                return context.getString(R.string.exception_atm_retry_otp);
            case ATM_CHARGE_SUCCESSFUL:
                return context.getString(R.string.exception_atm_charge_successful);
            case TRANS_INFO_NOT_FOUND:
                return context.getString(R.string.exception_trans_info_not_found);
            case ATM_CAPTCHA_INVALID:
                return context.getString(R.string.exception_atm_captcha_invalid);
            case ATM_COST_RATE_INVALID:
                return context.getString(R.string.exception_atm_cost_rate_invalid);
            case ITEMS_INVALID:
                return context.getString(R.string.exception_items_invalid);
            case HMAC_INVALID:
                return context.getString(R.string.exception_hmac_invalid);
            case TIME_INVALID:
                return context.getString(R.string.exception_time_invalid);
            case CAL_NET_CHARGE_AMT_FAIL:
                return context.getString(R.string.exception_cal_net_charge_amt_fail);
            case ATM_VERIFY_OTP_FAIL:
                return context.getString(R.string.exception_atm_verify_otp_fail);
            case APP_USER_INVALID:
                return context.getString(R.string.exception_app_user_invalid);
            case ZPW_GETTRANSID_FAIL:
                return context.getString(R.string.exception_zpw_gettransid_fail);
            case ZPW_PURCHASE_FAIL:
                return context.getString(R.string.exception_zpw_purchase_fail);
            case ZPW_ACCOUNT_NAME_INVALID:
                return context.getString(R.string.exception_zpw_account_name_invalid);
            case ZPW_ACCOUNT_SUSPENDED:
                return context.getString(R.string.exception_zpw_account_suspended);
            case ZPW_ACCOUNT_NOT_EXIST:
                return context.getString(R.string.exception_zpw_account_not_exist);
            case ZPW_BALANCE_NOT_ENOUGH:
                return context.getString(R.string.exception_zpw_balance_not_enough);
            case ZPW_GET_BALANCE_FAIL:
                return context.getString(R.string.exception_zpw_get_balance_fail);
            case ZPW_WRONG_PASSWORD:
                return context.getString(R.string.exception_zpw_wrong_password);
            case USER_INVALID:
                return context.getString(R.string.exception_user_invalid);
            case CARD_NOT_MATCH:
                return context.getString(R.string.exception_card_not_match);
            case TRANSID_FORMAT_INVALID:
                return context.getString(R.string.exception_transid_format_invalid);
            case CARD_TOKEN_INVALID:
                return context.getString(R.string.exception_card_token_invalid);
            case CARD_TOKEN_EXPIRE:
                return context.getString(R.string.exception_card_token_expire);
            case TRANSTYPE_INVALID:
                return context.getString(R.string.exception_transtype_invalid);
            case TRANSTYPE_INACTIVE:
                return context.getString(R.string.exception_transtype_inactive);
            case TRANSTYPE_MAINTENANCE:
                return context.getString(R.string.exception_transtype_maintenance);
            case APPTRANSID_GEN_ERROR:
                return context.getString(R.string.exception_apptransid_gen_error);
            case MAP_APPID_APPTRANSID_FAIL:
                return context.getString(R.string.exception_map_appid_apptransid_fail);
            case EXCEED_MAX_NOTIFY_WALLET_FEE:
                return context.getString(R.string.exception_exceed_max_notify_wallet_fee);
            case UPDATE_RESULT_FAIL_WALLET_FEE:
                return context.getString(R.string.exception_update_result_fail_wallet_fee);
            case APPTRANSID_INVALID:
                return context.getString(R.string.exception_apptransid_invalid);
            case TRANSTYPE_AMOUNT_INVALID:
                return context.getString(R.string.exception_transtype_amount_invalid);
            case CARD_ALREADY_MAP:
                return context.getString(R.string.exception_card_already_map);
            case SERVER_MAINTAIN:
                return context.getString(R.string.exception_server_maintain);
            case INVITATION_CODE_ERROR:
                return context.getString(R.string.exception_invitation_code_error);

        }
        return null;
    }
}
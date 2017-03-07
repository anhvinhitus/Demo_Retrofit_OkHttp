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
    public static final int USER_NOT_EXIST = -116;
    public static final int RECEIVER_IS_LOCKED = -131;
    public static final int SERVER_MAINTAIN = -999;
    public static final int INVITATION_CODE_ERROR = 24;
    public static final int INVITATION_CODE_INVALID = -142;
    public static final int USER_EXISTED = -150;
    public static final int OLD_PIN_NOT_MATCH = -157;
    public static final int WAITING_APPROVE_PROFILE_LEVEL_3 = -169;
    public static final int INCORRECT_PIN = -117;
    public static final int INCORRECT_PIN_LIMIT = -161;
    public static final int OTP_CHANGE_PASSWORF_WRONG = -114;
    public static final int USER_IS_LOCKED = -124;

    public static String getMessage(Context context, int errorCode) {
        int stringResourceId;
        switch (errorCode) {
            case SUCCESSFUL:
                stringResourceId = R.string.exception_successful;
                break;
            case EXCEPTION:
                stringResourceId = R.string.exception_exception;
                break;
            case ZK_NODE_EXIST_EXCEPTION:
                stringResourceId = R.string.exception_zk_node_exist_exception;
                break;
            case APPID_INVALID:
                stringResourceId = R.string.exception_appid_invalid;
                break;
            case APP_NOT_AVAILABLE:
                stringResourceId = R.string.exception_app_not_available;
                break;
            case APP_TIME_INVALID:
                stringResourceId = R.string.exception_app_time_invalid;
                break;
            case AMOUNT_INVALID:
                stringResourceId = R.string.exception_amount_invalid;
                break;
            case PLATFORM_INVALID:
                stringResourceId = R.string.exception_platform_invalid;
                break;
            case PLATFORM_NOT_AVAILABLE:
                stringResourceId = R.string.exception_platform_not_available;
                break;
            case DSCREEN_TYPE_INVALID:
                stringResourceId = R.string.exception_dscreen_type_invalid;
                break;
            case PMCID_INVALID:
                stringResourceId = R.string.exception_pmcid_invalid;
                break;
            case PMC_INACTIVE:
                stringResourceId = R.string.exception_pmc_inactive;
                break;
            case APPTRANSID_EXIST:
                stringResourceId = R.string.exception_apptransid_exist;
                break;
            case DUPLICATE_ZPTRANSID:
                stringResourceId = R.string.exception_duplicate_zptransid;
                break;
            case GET_TRANSID_FAIL:
                stringResourceId = R.string.exception_get_transid_fail;
                break;
            case SET_CACHE_FAIL:
                stringResourceId = R.string.exception_set_cache_fail;
                break;
            case GET_CACHE_FAIL:
                stringResourceId = R.string.exception_get_cache_fail;
                break;
            case UPDATE_RESULT_FAIL:
                stringResourceId = R.string.exception_update_result_fail;
                break;
            case EXCEED_MAX_NOTIFY:
                stringResourceId = R.string.exception_exceed_max_notify;
                break;
            case DEVICEID_NOT_MATCH:
                stringResourceId = R.string.exception_deviceid_not_match;
                break;
            case APPID_NOT_MATCH:
                stringResourceId = R.string.exception_appid_not_match;
                break;
            case PLATFORM_NOT_MATCH:
                stringResourceId = R.string.exception_platform_not_match;
                break;
            case PMC_FACTORY_NOT_FOUND:
                stringResourceId = R.string.exception_pmc_factory_not_found;
                break;
            case ZALO_LOGIN_FAIL:
                stringResourceId = R.string.exception_zalo_login_fail;
                break;
            case ZALO_LOGIN_EXPIRE:
                stringResourceId = R.string.exception_zalo_login_expire;
                break;
            case TOKEN_INVALID:
                stringResourceId = R.string.exception_token_invalid;
                break;
            case CARDINFO_INVALID:
                stringResourceId = R.string.exception_cardinfo_invalid;
                break;
            case CARDINFO_EXIST:
                stringResourceId = R.string.exception_cardinfo_exist;
                break;
            case SDK_INVALID:
                stringResourceId = R.string.exception_sdk_invalid;
                break;
            case CARDINFO_NOT_FOUND:
                stringResourceId = R.string.exception_cardinfo_not_found;
                break;
            case UM_TOKEN_NOT_FOUND:
                stringResourceId = R.string.exception_um_token_not_found;
                break;
            case ATM_CREATE_ORDER_DBG_FAIL:
                stringResourceId = R.string.exception_atm_create_order_dbg_fail;
                break;
            case UM_TOKEN_EXPIRE:
                stringResourceId = R.string.exception_um_token_expire;
                break;
            case REQUEST_FORMAT_INVALID:
                stringResourceId = R.string.exception_request_format_invalid;
                break;
            case CARD_INVALID:
                stringResourceId = R.string.exception_card_invalid;
                break;
            case APP_INACTIVE:
                stringResourceId = R.string.exception_app_inactive;
                break;
            case APP_MAINTENANCE:
                stringResourceId = R.string.exception_app_maintenance;
                break;
            case PMC_MAINTENANCE:
                stringResourceId = R.string.exception_pmc_maintenance;
                break;
            case PMC_NOT_AVAILABLE:
                stringResourceId = R.string.exception_pmc_not_available;
                break;
            case OVER_LIMIT:
                stringResourceId = R.string.exception_over_limit;
                break;
            case DUPLICATE:
                stringResourceId = R.string.exception_duplicate;
                break;
            case CREATE_ORDER_SUCCESSFUL:
                stringResourceId = R.string.exception_create_order_successful;
                break;
            case IN_NOTIFY_QUEUE:
                stringResourceId = R.string.exception_in_notify_queue;
                break;
            case PROCESSING:
                stringResourceId = R.string.exception_processing;
                break;
            case TRANS_NOT_FINISH:
                stringResourceId = R.string.exception_trans_not_finish;
                break;
            case ATM_WAIT_FOR_CHARGE:
                stringResourceId = R.string.exception_atm_wait_for_charge;
                break;
            case INIT:
                stringResourceId = R.string.exception_init;
                break;
            case USER_NOT_MATCH:
                stringResourceId = R.string.exception_user_not_match;
                break;
            case NOT_FOUND_SMS_SERVICE_PHONE:
                stringResourceId = R.string.exception_not_found_sms_service_phone;
                break;
            case MAX_RETRY_GET_DBG_STATUS:
                stringResourceId = R.string.exception_max_retry_get_dbg_status;
                break;
            case ATM_CREATE_ORDER_FAIL:
                stringResourceId = R.string.exception_atm_create_order_fail;
                break;
            case ATM_BANK_INVALID:
                stringResourceId = R.string.exception_atm_bank_invalid;
                break;
            case ATM_BANK_MAINTENANCE:
                stringResourceId = R.string.exception_atm_bank_maintenance;
                break;
            case DUPLICATE_APPTRANSID:
                stringResourceId = R.string.exception_duplicate_apptransid;
                break;
            case ATM_VERIFY_CARD_SUCCESSFUL:
                stringResourceId = R.string.exception_atm_verify_card_successful;
                break;
            case ATM_VERIFY_OTP_SUCCESS:
                stringResourceId = R.string.exception_atm_verify_otp_success;
                break;
            case ATM_VERIFY_CARD_FAIL:
                stringResourceId = R.string.exception_atm_verify_card_fail;
                break;
            case ATM_MAX_RETRY_OTP_FAIL:
                stringResourceId = R.string.exception_atm_max_retry_otp_fail;
                break;
            case ATM_QUERY_ORDER_FAIL:
                stringResourceId = R.string.exception_atm_query_order_fail;
                break;
            case ATM_BANK_SRC_INVALID:
                stringResourceId = R.string.exception_atm_bank_src_invalid;
                break;
            case DESERIALIZE_TRANS_FAIL:
                stringResourceId = R.string.exception_deserialize_trans_fail;
                break;
            case IN_GET_STATUS_ATM_QUEUE:
                stringResourceId = R.string.exception_in_get_status_atm_queue;
                break;
            case ATM_CHARGE_FAIL:
                stringResourceId = R.string.exception_atm_charge_fail;
                break;
            case ATM_RETRY_CAPTCHA:
                stringResourceId = R.string.exception_atm_retry_captcha;
                break;
            case ATM_RETRY_OTP:
                stringResourceId = R.string.exception_atm_retry_otp;
                break;
            case ATM_CHARGE_SUCCESSFUL:
                stringResourceId = R.string.exception_atm_charge_successful;
                break;
            case TRANS_INFO_NOT_FOUND:
                stringResourceId = R.string.exception_trans_info_not_found;
                break;
            case ATM_CAPTCHA_INVALID:
                stringResourceId = R.string.exception_atm_captcha_invalid;
                break;
            case ATM_COST_RATE_INVALID:
                stringResourceId = R.string.exception_atm_cost_rate_invalid;
                break;
            case ITEMS_INVALID:
                stringResourceId = R.string.exception_items_invalid;
                break;
            case HMAC_INVALID:
                stringResourceId = R.string.exception_hmac_invalid;
                break;
            case TIME_INVALID:
                stringResourceId = R.string.exception_time_invalid;
                break;
            case CAL_NET_CHARGE_AMT_FAIL:
                stringResourceId = R.string.exception_cal_net_charge_amt_fail;
                break;
            case ATM_VERIFY_OTP_FAIL:
                stringResourceId = R.string.exception_atm_verify_otp_fail;
                break;
            case APP_USER_INVALID:
                stringResourceId = R.string.exception_app_user_invalid;
                break;
            case ZPW_GETTRANSID_FAIL:
                stringResourceId = R.string.exception_zpw_gettransid_fail;
                break;
            case ZPW_PURCHASE_FAIL:
                stringResourceId = R.string.exception_zpw_purchase_fail;
                break;
            case ZPW_ACCOUNT_NAME_INVALID:
                stringResourceId = R.string.exception_zpw_account_name_invalid;
                break;
            case ZPW_ACCOUNT_SUSPENDED:
                stringResourceId = R.string.exception_zpw_account_suspended;
                break;
            case ZPW_ACCOUNT_NOT_EXIST:
                stringResourceId = R.string.exception_zpw_account_not_exist;
                break;
            case ZPW_BALANCE_NOT_ENOUGH:
                stringResourceId = R.string.exception_zpw_balance_not_enough;
                break;
            case ZPW_GET_BALANCE_FAIL:
                stringResourceId = R.string.exception_zpw_get_balance_fail;
                break;
            case ZPW_WRONG_PASSWORD:
                stringResourceId = R.string.exception_zpw_wrong_password;
                break;
            case USER_INVALID:
                stringResourceId = R.string.exception_user_invalid;
                break;
            case CARD_NOT_MATCH:
                stringResourceId = R.string.exception_card_not_match;
                break;
            case TRANSID_FORMAT_INVALID:
                stringResourceId = R.string.exception_transid_format_invalid;
                break;
            case CARD_TOKEN_INVALID:
                stringResourceId = R.string.exception_card_token_invalid;
                break;
            case CARD_TOKEN_EXPIRE:
                stringResourceId = R.string.exception_card_token_expire;
                break;
            case TRANSTYPE_INVALID:
                stringResourceId = R.string.exception_transtype_invalid;
                break;
            case TRANSTYPE_INACTIVE:
                stringResourceId = R.string.exception_transtype_inactive;
                break;
            case TRANSTYPE_MAINTENANCE:
                stringResourceId = R.string.exception_transtype_maintenance;
                break;
            case APPTRANSID_GEN_ERROR:
                stringResourceId = R.string.exception_apptransid_gen_error;
                break;
            case MAP_APPID_APPTRANSID_FAIL:
                stringResourceId = R.string.exception_map_appid_apptransid_fail;
                break;
            case EXCEED_MAX_NOTIFY_WALLET_FEE:
                stringResourceId = R.string.exception_exceed_max_notify_wallet_fee;
                break;
            case UPDATE_RESULT_FAIL_WALLET_FEE:
                stringResourceId = R.string.exception_update_result_fail_wallet_fee;
                break;
            case APPTRANSID_INVALID:
                stringResourceId = R.string.exception_apptransid_invalid;
                break;
            case TRANSTYPE_AMOUNT_INVALID:
                stringResourceId = R.string.exception_transtype_amount_invalid;
                break;
            case CARD_ALREADY_MAP:
                stringResourceId = R.string.exception_card_already_map;
                break;
            case SERVER_MAINTAIN:
                stringResourceId = R.string.exception_server_maintain;
                break;
            case INVITATION_CODE_ERROR:
                stringResourceId = R.string.exception_invitation_code_error;
                break;
            case INVITATION_CODE_INVALID:
                stringResourceId = R.string.exception_invitation_code_invalid;
                break;
            case USER_IS_LOCKED:
                stringResourceId = R.string.exception_zpw_account_suspended;
                break;
            default:
                return null;
        }

        return context.getString(stringResourceId);
    }
}

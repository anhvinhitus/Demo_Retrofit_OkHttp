package vn.com.zalopay.wallet.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import vn.com.zalopay.wallet.business.data.RS;

public class Constants {
    public static final int VCB_MAX_RETRY_GET_NUMBERPHONE = 3;
    public static final String AUTOFILL_OTP_WEBFLOW_JS = "vcb_autofill_otp.js";
    public static final String AUTO_SELECT_SERVICE_JS = "vcb_initConfirmBefore.js";
    public static final String COMMA = ",";
    public static final String UNDERLINE = "_";
    public static final String URL_PLATFORM_INFO = "v001/tpe/v001getplatforminfo";
    public static final String URL_SUBMIT_ORDER = "v001/tpe/submittrans";
    public static final String URL_GET_STATUS = "v001/tpe/gettransstatus";
    public static final String URL_CHECK_SUBMIT_ORDER_STATUS = "v001/tpe/getstatusbyapptransidforclient";
    public static final String URL_SAVE_CARD = "v001/tpe/mapcard";
    public static final String URL_GETBALANCE_ZALOPAY = "v001/tpe/getbalance";
    public static final String URL_APP_INFO = "v001/tpe/getappinfov1";
    public static final String URL_REMOVE_MAPCARD = "v001/tpe/removemapcard";
    public static final String URL_GET_BANKLIST = "v001/tpe/getbanklist";
    public static final String URL_ATM_AUTHEN = "v001/tpe/atmauthenpayer";
    public static final String URL_TRACKING_LOG = "v001/tpe/sdkwriteatmtime";
    public static final String URL_VERIFY_CARDMAP = "v001/tpe/verifycardformapping";
    public static final String URL_GET_STATUS_MAPCARD = "v001/tpe/getstatusmapcard";
    public static final String URL_AUTHEN_CARD_MAP = "v001/tpe/authcardholderformapping";
    public static final String URL_REPORT_ERROR = "v001/tpe/sdkerrorreport";
    public static final String URL_SUBMIT_MAP_ACCOUNT = "v001/tpe/submitmapaccount";
    public static final String URL_LISTCARDINFO = "/um/listcardinfoforclient";
    public static final String URL_LISTBANKACCCOUNT = "/um/listbankaccountforclient";
    public static final int REQUEST_CODE_SMS = 101;
    public static final String BITMAP_EXTENSION = ".png";
    public static final String SUPPORT_INTRO_ACTION_FEEDBACK = "vn.com.vng.zalopay.action.FEEDBACK";
    public static final String SUPPORT_INTRO_ACTION_SUPPORT_CENTER = "vn.com.vng.zalopay.action.SUPPORT_CENTER";
    public static final int TIMES_DELAY_TO_GET_NOTIFY = 5000;//ms
    public static final int MAX_INTERVAL_OF_RETRY = 30000;//ms
    public static final int SLEEPING_INTERVAL_OF_RETRY = 1000;//ms
    public static final int API_CONNECTING_REQUEST_TIMEOUT = 10000;//ms
    public static final int API_READ_REQUEST_TIMEOUT = 5000;//ms
    public static final int API_PLATFORM_TIMEOUT = 30000;//ms
    public static final int API_MAX_RETRY = 3;
    public static final int API_DELAY_RETRY = 500;//ms
    public static final int TRANS_STATUS_DELAY_RETRY = 1000;//ms
    public static final int TRANS_STATUS_MAX_RETRY = 30;
    public static final int PLATFORM_RETRY_INTERVAL = 7000;//ms.
    public static final int PLATFORM_MAX_RETRY = 10;
    public static final int DOWNLOAD_RESOURCE_RETRY_INTERVAL = 10000;//ms.
    public static final int DOWNLOAD_RESOURCE_MAX_RETRY = 10;
    public static final long GETSTATUS_CLIENT_COUNT = 5;
    public static final int MAX_RETRY_GETSTATUS = 5;
    public static final int MIN_CC_LENGTH = 6;
    public static final int MIN_ATM_LENGTH = 6;
    public static final int MAX_COUNT_RETRY_PIN = 5;
    public static final int MAX_COUNT_RETRY_CAPTCHA = 5;
    public static final int MAX_RETRY_OPEN_NETWORKING = 3;
    public static final int PIN_WRONG_RETURN_CODE = -117;
    public static final int CARD_ALREADY_MAP = -164;
    public static final int SERVER_MAINTENANCE_CODE = -999;
    public static final int UPGRADE_LEVEL_CODE = -119;
    public static final List<Integer> MONEY_NOT_ENOUGH_CODE = Arrays.asList(-62, -63);
    public static final List<Integer> PAYMENT_LIMIT_PER_DAY_CODE = Collections.singletonList(-133);
    public static final int TRANSACTION_PROCESSING = 5;
    public static final int AUTHEN_PAYER_OTP_WRONG_CODE = 17;
    public static final List<Integer> GET_STATUS_AUTHEN_PAYER_CODE = Arrays.asList(14, 18);
    public static final List<Long> TRANSACTION_SUCCESS_NOTIFICATION_TYPES = Arrays.asList(1L, 2L, 3L, 4L, 5L);//filter list notification app push notification into sdk
    //user submited order to server fail
    public static final int TRANSACTION_NOT_SUBMIT = -49;
    public static final int FORCE_UP_VERSION_CODE = -1001;
    public static final int UP_VERSION_CODE = -1002;
    public static final int LEVELMAP_INVALID = -1;
    public static final int LEVELMAP_BAN = 1;
    public static final int LEVELMAP_ALLOW = 0;
    public static final int INPUT_INVALID = -1;
    public static final int REQUIRE_PIN = 1;
    public static final int REQUIRE_OTP = 2;
    public static final int RECEIVER = -888;
    public static final int API = -999;
    public static final String SHOW_DIALOG = "show";
    public static final String MESSAGE = "mess";
    public static int PAYMENT_INIT = 1;
    public static int INPUT_CARDINFO_PHARSE = 2;
    public static int ORDER_SUBMIT = 3;
    public static int CAPTCHA_PHARSE = 4;
    public static int OTP_PHARSE = 5;
    public static int PIN_PHARSE = 6;
    public static int STATUS_PHARSE = 7;
    public static int RESULT_PHARSE = 8;
    public static int UNDEFINE = 9;
    public static int NULL_ERRORCODE = -747;
    public static String TRUE = "true";

    public static final String PAGE_SUCCESS = RS.layout.screen__success;
    public static final String PAGE_FAIL = RS.layout.screen__fail;
    public static final String PAGE_FAIL_NETWORKING = RS.layout.screen__fail_networking;
    public static final String PAGE_FAIL_PROCESSING = RS.layout.screen__fail_processing;
    public static final String PAGE_BALANCE_ERROR = RS.layout.screen__zalopay__balance_error;
    public static final String SCREEN_CC = RS.layout.screen__card;
    public static final String SCREEN_ATM = RS.layout.screen__card;
    public static final String PAGE_AUTHEN = RS.layout.screen__local__card__authen;
    public static final String PAGE_COVER_BANK_AUTHEN = RS.layout.screen__cover__bank__authen;
    public static final String PAGE_SELECTION_ACCOUNT_BANK = RS.layout.screen_selection_account_list;

    public static final String VCB_LOGIN_PAGE = "zpsdk_atm_vcb_login_page";
    public static final String VCB_REGISTER_PAGE = "zpsdk_atm_vcb_register_page";
    public static final String VCB_UNREGISTER_PAGE = "zpsdk_atm_vcb_unregister_page";
    public static final String VCB_REGISTER_COMPLETE_PAGE = "zpsdk_atm_vcb_register_complete_page";
    public static final String VCB_UNREGISTER_COMPLETE_PAGE = "zpsdk_atm_vcb_unregister_complete_page";
    public static final String VCB_REFRESH_CAPTCHA = "zpsdk_atm_vcb_refresh_captcha";
    public static final String SCREEN_LINK_ACC = RS.layout.screen__link__acc;
    public static final String PAGE_VCB_LOGIN = RS.layout.screen__vcb__login;
    public static final String PAGE_VCB_CONFIRM_LINK = RS.layout.screen__vcb__confirm_link;
    public static final String PAGE_VCB_OTP = RS.layout.screen_vcb_otp;
    public static final String PAGE_VCB_CONFIRM_UNLINK = RS.layout.screen__vcb__confirm_unlink;
    public static final String PAGE_LINKACC_SUCCESS = RS.layout.screen__linkacc__success;
    public static final String PAGE_LINKACC_FAIL = RS.layout.screen__linkacc__fail;
    public static final String PAGE_UNLINKACC_SUCCESS = RS.layout.screen__unlinkacc__success;
    public static final String PAGE_UNLINKACC_FAIL = RS.layout.screen__unlinkacc__fail;

    public static final String PMC_CONFIG = "config";
    public static final String STATUS_RESPONSE = "status";
}

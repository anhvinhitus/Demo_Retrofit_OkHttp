package vn.com.zalopay.wallet.business.data;

import java.util.Arrays;
import java.util.List;

public class Constants {
    public static final String COMMA = ",";
    public static final String UNDERLINE = "_";
    public static final String URL_PLATFORM_INFO = "v001/tpe/v001getplatforminfo";
    public static final String URL_SUBMIT_ORDER = "v001/tpe/submittrans";
    public static final String URL_GET_STATUS = "v001/tpe/gettransstatus";
    public static final String URL_CHECK_SUBMIT_ORDER_STATUS = "v001/tpe/getstatusbyapptransidforclient";
    public static final String URL_SAVE_CARD = "v001/tpe/mapcard";
    public static final String URL_GETBALANCE_ZALOPAY = "v001/tpe/getbalance";
    public static final String URL_APP_INFO = "v001/tpe/getappinfo";
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
    public static final int NOTIFICATION_NETWORKING_ID = 1001;
    public static final int REQUEST_CODE_SMS = 101;
    public static final String BITMAP_EXTENSION = ".png";
    public static final String FILTER_ACTION_BANK_SMS_RECEIVER = "ACTION_BANK_SMS_COME";
    public static final String BANK_SMS_RECEIVER_SENDER = "BANK_SMS_RECEIVER_SENDER";
    public static final String BANK_SMS_RECEIVER_BODY = "BANK_SMS_RECEIVER_BODY";
    public static final String FILTER_ACTION_NETWORKING_CHANGED = "ACTION_NETWORKING_CHANGE";
    public static final String ACTION_UNLOCK_SCREEN = "ACTION_UNLOCK_SCREEN";
    public static final String NETWORKING_NOT_STABLE = "NETWORKING_NOT_STABLE";
    public static final String SUPPORT_INTRO_ACTION_FEEDBACK = "vn.com.vng.zalopay.action.FEEDBACK";
    public static final String SUPPORT_INTRO_ACTION_SUPPORT_CENTER = "vn.com.vng.zalopay.action.SUPPORT_CENTER";
    public static final String SCREENSHOT = "screenshot";
    public static final String TRANSACTIONID = "transactionid";
    public static final String CATEGORY = "category";
    public static final String DESCRIPTION = "description";
    public static final String ERRORCODE = "errorcode";
    public static final int TIMES_DELAY_TO_GET_NOTIFY = 5000;//ms
    public static final int MAX_INTERVAL_OF_RETRY = 30000;//ms
    public static final int SLEEPING_INTERVAL_OF_RETRY = 1000;//ms
    public static final int API_CONNECTING_REQUEST_TIMEOUT = 10000;//ms
    public static final int API_READ_REQUEST_TIMEOUT = 5000;//ms
    public static final int PROGRESS_DIALOG_TIMEOUT = 35000;//ms
    public static final int API_PLATFORM_TIMEOUT = 30000;//ms
    public static final int API_MAX_RETRY = 3;
    public static final int PLATFORM_RETRY_INTERVAL = 7000;//ms.
    public static final int PLATFORM_MAX_RETRY = 10;
    public static final int DOWNLOAD_RESOURCE_RETRY_INTERVAL = 10000;//ms.
    public static final int DOWNLOAD_RESOURCE_MAX_RETRY = 10;
    public static final long GETSTATUS_CLIENT_COUNT = 5;
    public static final int MAX_RETRY_GETSTATUS = 5;
    public static final String CCCode = "123PCC";
    public static final int MIN_CC_LENGTH = 6;
    public static final int MIN_ATM_LENGTH = 6;
    public static final int MAX_COUNT_RETRY_PIN = 5;
    public static final int MAX_COUNT_RETRY_CAPTCHA = 5;
    public static final int MAX_COUNT_RETRY_SAVE_CARD = 3;
    public static final int MAX_RETRY_OPEN_NETWORKING = 3;
    public static final int PIN_WRONG_RETURN_CODE = -117;
    public static final int CARD_ALREADY_MAP = -164;
    public static final int SERVER_MAINTENANCE_CODE = -999;
    public static final int UPGRADE_LEVEL_CODE = -119;
    public static final List<Integer> MONEY_NOT_ENOUGH_CODE = Arrays.asList(-62, -63);
    public static final List<Integer> PAYMENT_LIMIT_PER_DAY_CODE = Arrays.asList(-133);
    public static final int TRANSACTION_PROCESSING = 5;
    public static final int AUTHEN_PAYER_OTP_WRONG_CODE = 17;
    public static final List<Integer> GET_STATUS_AUTHEN_PAYER_CODE = Arrays.asList(14, 18);
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

    public static enum HostType {
        LIVE,
        STAGING,
        SANDBOX
    }

    public static class NOTIFY_TYPE {
        public final static int LINKACC = 116;
        public final static int UNLINKACC = 115;
    }

}

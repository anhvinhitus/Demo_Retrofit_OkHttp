package vn.com.zalopay.wallet.business.error;

import android.util.SparseArray;

import vn.com.zalopay.wallet.constants.PaymentStatus;

/***
 * error code map table
 */
public class ErrorManager {
    public static final SparseArray<String> mErrorArray = new SparseArray<>();
    public static final SparseArray<String> mErrorLoginArray = new SparseArray<>();
    public static final SparseArray<String> mErrorAccountArray = new SparseArray<>();

    static {

        mErrorArray.put(-1, "ZK_NODE_EXIST_EXCEPTION");
        mErrorArray.put(-2, "APPID_INVALID");
        mErrorArray.put(-3, "APP_NOT_AVAILABLE");
        mErrorArray.put(-4, "APP_TIME_INVALID");
        mErrorArray.put(-5, "AMOUNT_INVALID");
        mErrorArray.put(-6, "PLATFORM_INVALID");
        mErrorArray.put(-7, "PLATFORM_NOT_AVAILABLE");
        mErrorArray.put(-8, "DSCREEN_TYPE_INVALID");
        mErrorArray.put(-70, "APPTRANSID_EXIST");
        mErrorArray.put(-79, "DUPLICATE_ZPTRANSID");
        mErrorArray.put(-18, "DEVICEID_NOT_MATCH");
        mErrorArray.put(-19, "APPID_NOT_MATCH");
        mErrorArray.put(-20, "PLATFORM_NOT_MATCH");
        mErrorArray.put(-21, "PMC_FACTORY_NOT_FOUND");
        mErrorArray.put(-32, "APP_INACTIVE");
        mErrorArray.put(-33, "APP_MAINTENANCE");
        mErrorArray.put(-36, "OVER_LIMIT");
        mErrorArray.put(-81, "USER_NOT_MATCH");
        mErrorArray.put(-40, "MAX_RETRY_GET_DBG_STATUS");
        mErrorArray.put(-48, "WRONG_PASSWORD");
        mErrorArray.put(-68, "DUPLICATE_APPTRANSID");
        mErrorArray.put(-52, "ITEMS_INVALID");
        mErrorArray.put(-53, "HMAC_INVALID");
        mErrorArray.put(-54, "TIME_INVALID");
        mErrorArray.put(-57, "APP_USER_INVALID");
        mErrorArray.put(-66, "USER_INVALID");
        mErrorArray.put(-83, "TRANSID_FORMAT_INVALID");
        mErrorArray.put(-84, "CARD_TOKEN_INVALID");
        mErrorArray.put(-85, "CARD_TOKEN_EXPIRE");
        mErrorArray.put(-89, "MAP_APPID_APPTRANSID_FAIL");
        mErrorArray.put(-92, "APPTRANSID_INVALID");
        mErrorArray.put(-60, "ZPW_ACCOUNT_NAME_INVALID");
        mErrorArray.put(-61, "ZPW_ACCOUNT_SUSPENDED");
        mErrorArray.put(-62, "ZPW_ACCOUNT_NOT_EXIST");
        mErrorArray.put(-63, "ZPW_BALANCE_NOT_ENOUGH");
        mErrorArray.put(2, "DUPLICATE");
        mErrorArray.put(5, "PROCESSING");

        mErrorLoginArray.put(-71, "ZALO_LOGIN_FAIL");
        mErrorLoginArray.put(-72, "ZALO_LOGIN_EXPIRE");
        mErrorLoginArray.put(-73, "TOKEN_INVALID");
        mErrorLoginArray.put(-77, "UM_TOKEN_NOT_FOUND");
        mErrorLoginArray.put(-78, "UM_TOKEN_EXPIRE");

        mErrorAccountArray.put(-124, "USER_IS_LOCKED");

    }

    public static boolean shouldShowDialog(@PaymentStatus int status) {
        return status != PaymentStatus.TOKEN_EXPIRE && status != PaymentStatus.USER_LOCK;
    }

    public static boolean needToTerminateTransaction(@PaymentStatus int status) {
        return status == PaymentStatus.PROCESSING
                || status == PaymentStatus.LEVEL_UPGRADE_PASSWORD
                || status == PaymentStatus.UPLEVEL_AND_LINK_BANKACCOUNT_AND_PAYMENT
                || status == PaymentStatus.SUCCESS
                || status == PaymentStatus.TOKEN_EXPIRE
                || status == PaymentStatus.USER_LOCK
                || status == PaymentStatus.MONEY_NOT_ENOUGH
                || status == PaymentStatus.INVALID_DATA
                || status == PaymentStatus.SERVICE_MAINTENANCE;
    }
}

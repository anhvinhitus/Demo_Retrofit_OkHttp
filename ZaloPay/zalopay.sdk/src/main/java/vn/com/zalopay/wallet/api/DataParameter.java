package vn.com.zalopay.wallet.api;

import android.os.Build;
import android.text.TextUtils;

import java.util.Map;

import timber.log.Timber;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.utility.DeviceUtil;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.api.task.SDKReportTask;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.base.PaymentLocation;
import vn.com.zalopay.wallet.business.entity.base.ZPWRemoveMapCardParams;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.constants.ConstantParams;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;

public class DataParameter {
    /**
     * Add param Sendlog
     *
     * @param params
     * @param pmcID
     * @param pTransID
     * @param pCaptchaBeginTime
     * @param pCaptchaEndTime
     * @param pOtpBeginTime
     * @param pOtpEndTime
     */
    public static void prepareSendLog(Map<String, String> params, String pUserId, String pAccessToken, int pmcID, String pTransID, long pCaptchaBeginTime, long pCaptchaEndTime, long pOtpBeginTime, long pOtpEndTime) throws Exception {
        putBase(params);
        params.put(ConstantParams.USER_ID, pUserId);
        params.put(ConstantParams.ACCESS_TOKEN, pAccessToken);
        params.put(ConstantParams.PMC_ID, String.valueOf(pmcID));
        params.put(ConstantParams.TRANS_ID, pTransID);
        params.put(ConstantParams.ATM_CAPTCHA_BEGINDATE, String.valueOf(pCaptchaBeginTime));
        params.put(ConstantParams.ATM_CAPTCHA_ENDDATE, String.valueOf(pCaptchaEndTime));
        params.put(ConstantParams.ATM_OTP_BEGINDATE, String.valueOf(pOtpBeginTime));
        params.put(ConstantParams.ATM_OTP_ENDDATE, String.valueOf(pOtpEndTime));

    }

    /**
     * Add params ATM AuthenPayerTask
     *
     * @param params
     * @param pTransID
     * @param pAuthenType
     * @param pAuthenValue
     */
    public static void prepareAtmAuthenPayer(Map<String, String> params, String pUserId, String pAccessToken, String pTransID, String pAuthenType, String pAuthenValue) throws Exception {
        putBase(params);
        params.put(ConstantParams.USER_ID, pUserId);
        params.put(ConstantParams.ACCESS_TOKEN, pAccessToken);
        params.put(ConstantParams.ZP_TRANSID, pTransID);
        params.put(ConstantParams.AUTHEN_TYPE, pAuthenType);
        params.put(ConstantParams.AUTHEN_VALUE, pAuthenValue);
    }

    /**
     * Add param RemoveMapCardTask
     *
     * @param params
     * @param pCard
     */
    public static void prepareRemoveCard(Map<String, String> params, ZPWRemoveMapCardParams pCard) {
        putBase(params);
        params.put(ConstantParams.USER_ID, pCard.userID);
        params.put(ConstantParams.ACCESS_TOKEN, pCard.accessToken);
        params.put(ConstantParams.CARD_NAME, pCard.mapCard.cardname);
        params.put(ConstantParams.FIRST6_CARDNO, pCard.mapCard.first6cardno);
        params.put(ConstantParams.LAST4_CARDNO, pCard.mapCard.last4cardno);
        params.put(ConstantParams.BANK_CODE, TextUtils.isEmpty(pCard.mapCard.bankcode) ? "" : pCard.mapCard.bankcode);
    }


    public static boolean prepareSDKReport(Map<String, String> params, String pUserId, String pAccessToken, String pTranID, String pBankCode, int pExInfo, String pException) {
        try {
            putBase(params);
            params.put(ConstantParams.USER_ID, pUserId);
            params.put(ConstantParams.ACCESS_TOKEN, pAccessToken);
            params.put(ConstantParams.TRANSID, !TextUtils.isEmpty(pTranID) ? pTranID : "");
            params.put(ConstantParams.BANK_CODE, !TextUtils.isEmpty(pBankCode) ? pBankCode : "");
            params.put(ConstantParams.EXINFO, (pExInfo != SDKReportTask.DEFAULT) ? String.valueOf(pExInfo) : "");
            params.put(ConstantParams.EXCEPTION, !TextUtils.isEmpty(pException) ? pException : "");
            return true;
        } catch (Exception ex) {
            Timber.d("prepareSDKReport: %s", ex.getMessage());
            return false;
        }
    }

    /**
     * Add params Get Status
     *
     * @param params
     * @param pTransID
     */
    public static void prepareGetStatusParams(String pAppId, UserInfo pUserInfo, Map<String, String> params, String pTransID) {
        putBase(params);
        params.put(ConstantParams.APP_ID, pAppId);
        params.put(ConstantParams.ACCESS_TOKEN, pUserInfo.accesstoken);
        params.put(ConstantParams.ZP_TRANSID, pTransID);
        params.put(ConstantParams.USER_ID, pUserInfo.zalopay_userid);
        params.put(ConstantParams.DEVICE_ID, DeviceUtil.getUniqueDeviceID(GlobalData.getAppContext()));
    }

    /***
     * params for get transaction status in network error case
     *
     * @param params
     * @param pAppTransID
     */
    public static void prepareGetStatusByAppStransParams(String appID, String zalopay_userid, String pAppTransID, Map<String, String> params) {
        putBase(params);
        params.put(ConstantParams.APP_ID, appID);
        params.put(ConstantParams.APP_TRANS_ID, pAppTransID);
        params.put(ConstantParams.USER_ID, zalopay_userid);
    }

    public static void prepareGetStatusMapCardParams(Map<String, String> params, UserInfo pUserInfo, String pTransID) throws Exception {
        putBase(params);
        params.put(ConstantParams.ACCESS_TOKEN, pUserInfo.accesstoken);
        params.put(ConstantParams.ZP_TRANSID, pTransID);
        params.put(ConstantParams.USER_ID, pUserInfo.zalopay_userid);
    }

    public static void prepareVerifyMapCardParams(AdapterBase pAdapter, Map<String, String> params) throws Exception {
        UserInfo userInfo = pAdapter.getPaymentInfoHelper().getUserInfo();
        params.put(ConstantParams.ACCESS_TOKEN, userInfo.accesstoken);
        params.put(ConstantParams.USER_ID, userInfo.zalopay_userid);
        params.put(ConstantParams.ZALO_ID, userInfo.zalo_userid);
        params.put(ConstantParams.PLATFORM, BuildConfig.PAYMENT_PLATFORM);
        params.put(ConstantParams.DEVICE_ID, DeviceUtil.getUniqueDeviceID(GlobalData.getAppContext()));
        params.put(ConstantParams.DEVICE_MODEL, DeviceUtil.getDeviceName());
        params.put(ConstantParams.APP_VERSION, SdkUtils.getAppVersion(GlobalData.getAppContext()));
        params.put(ConstantParams.SDK_VERSION, BuildConfig.SDK_BUILD_VERSION);
        params.put(ConstantParams.OS_VERSION, Build.VERSION.RELEASE);
        params.put(ConstantParams.CONN_TYPE, ConnectionUtil.getConnectionType(GlobalData.getAppContext()));
        params.put(ConstantParams.MNO, ConnectionUtil.getSimOperator(GlobalData.getAppContext()));
        params.put(ConstantParams.CARDINFO, GsonUtils.toJsonString(pAdapter.getCard()));
    }

    public static boolean prepareSubmitTransactionParams(int channelId, long appId, String charge_info, String hashPassword, AbstractOrder order, UserInfo userInfo,
                                                         PaymentLocation location, @TransactionType int transtype,
                                                         Map<String, String> params) {

        params.put(ConstantParams.PMC_ID, String.valueOf(channelId));
        params.put(ConstantParams.CHARGE_INFO, !TextUtils.isEmpty(charge_info) ? charge_info : "");
        params.put(ConstantParams.PIN, !TextUtils.isEmpty(hashPassword) ? hashPassword : "");
        params.put(ConstantParams.TRANS_TYPE, String.valueOf(transtype));
        params.put(ConstantParams.ACCESS_TOKEN, userInfo.accesstoken);
        params.put(ConstantParams.USER_ID, userInfo.zalopay_userid);
        params.put(ConstantParams.ZALO_ID, userInfo.zalo_userid);

        double lat = 0, lng = 0;
        if (location != null) {
            lat = location.latitude;
            lng = location.longitude;
        }
        params.put(ConstantParams.LATTITUDE, String.valueOf(lat));
        params.put(ConstantParams.LONGITUDE, String.valueOf(lng));

        params.put(ConstantParams.ORDER_SOURCE, String.valueOf(order.ordersource));

        params.put(ConstantParams.APP_ID, String.valueOf(appId));
        params.put(ConstantParams.APP_TRANS_ID, order.apptransid);
        params.put(ConstantParams.APP_USER, order.appuser);
        params.put(ConstantParams.APP_TIME, String.valueOf(order.apptime));
        params.put(ConstantParams.ITEM, order.item);
        params.put(ConstantParams.DESCRIPTION, order.description);
        params.put(ConstantParams.EMBED_DATA, order.embeddata);
        params.put(ConstantParams.MAC, order.mac);
        params.put(ConstantParams.PLATFORM, BuildConfig.PAYMENT_PLATFORM);
        params.put(ConstantParams.PLATFORM_CODE, BuildConfig.PAYMENT_PLATFORM);
        params.put(ConstantParams.AMOUNT, String.valueOf(order.amount));
        params.put(ConstantParams.DEVICE_ID, DeviceUtil.getUniqueDeviceID(GlobalData.getAppContext()));
        params.put(ConstantParams.DEVICE_MODEL, DeviceUtil.getDeviceName());
        params.put(ConstantParams.APP_VERSION, SdkUtils.getAppVersion(GlobalData.getAppContext()));
        params.put(ConstantParams.SDK_VERSION, BuildConfig.SDK_BUILD_VERSION);
        params.put(ConstantParams.OS_VERSION, Build.VERSION.RELEASE);
        params.put(ConstantParams.CONN_TYPE, ConnectionUtil.getConnectionType(GlobalData.getAppContext()));
        params.put(ConstantParams.MNO, ConnectionUtil.getSimOperator(GlobalData.getAppContext()));
        return true;
    }

    public static void putBase(Map<String, String> params) {
        params.put(ConstantParams.APP_VERSION, SdkUtils.getAppVersion(GlobalData.getAppContext()));
    }

    /**
     * Add params Base
     *
     * @param params
     */
    public static void prepareMapAccountParams(Map<String, String> params, String pBankAccInfo, UserInfo pUserInfo) throws Exception {
        params.put(ConstantParams.USER_ID, pUserInfo.zalopay_userid);
        params.put(ConstantParams.ZALO_ID, pUserInfo.zalo_userid);
        params.put(ConstantParams.ACCESS_TOKEN, pUserInfo.accesstoken);
        params.put(ConstantParams.BANK_ACCOUNT_INFO, String.valueOf(pBankAccInfo));
        params.put(ConstantParams.PLATFORM, BuildConfig.PAYMENT_PLATFORM);
        params.put(ConstantParams.DEVICE_ID, DeviceUtil.getUniqueDeviceID(GlobalData.getAppContext()));
        params.put(ConstantParams.APP_VERSION, SdkUtils.getAppVersion(GlobalData.getAppContext()));
        params.put(ConstantParams.MNO, ConnectionUtil.getSimOperator(GlobalData.getAppContext()));
        params.put(ConstantParams.SDK_VERSION, BuildConfig.SDK_BUILD_VERSION);
        params.put(ConstantParams.OS_VERSION, Build.VERSION.RELEASE);
        params.put(ConstantParams.DEVICE_MODEL, DeviceUtil.getDeviceName());
        params.put(ConstantParams.CONN_TYPE, ConnectionUtil.getConnectionType(GlobalData.getAppContext()));
    }
}

package vn.com.zalopay.wallet.datasource;

import android.os.Build;
import android.text.TextUtils;

import java.util.Map;

import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.ConstantParams;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.business.entity.base.ZPWRemoveMapCardParams;
import vn.com.zalopay.wallet.business.entity.creditcard.DMappedCreditCard;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.datasource.task.SDKReportTask;
import vn.com.zalopay.wallet.utils.ConnectionUtil;
import vn.com.zalopay.wallet.utils.DeviceUtil;
import vn.com.zalopay.wallet.utils.DimensionUtil;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.SdkUtils;

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
    public static void prepareSendLog(Map<String, String> params, int pmcID, String pTransID, long pCaptchaBeginTime, long pCaptchaEndTime, long pOtpBeginTime, long pOtpEndTime) throws Exception {
        putBase(params);
        params.put(ConstantParams.USER_ID, GlobalData.getPaymentInfo().userInfo.zaloPayUserId);
        params.put(ConstantParams.ACCESS_TOKEN, GlobalData.getPaymentInfo().userInfo.accessToken);
        params.put(ConstantParams.PMC_ID, String.valueOf(pmcID));
        params.put(ConstantParams.TRANS_ID, pTransID);
        params.put(ConstantParams.ATM_CAPTCHA_BEGINDATE, String.valueOf(pCaptchaBeginTime));
        params.put(ConstantParams.ATM_CAPTCHA_ENDDATE, String.valueOf(pCaptchaEndTime));
        params.put(ConstantParams.ATM_OTP_BEGINDATE, String.valueOf(pOtpBeginTime));
        params.put(ConstantParams.ATM_OTP_ENDDATE, String.valueOf(pOtpEndTime));

    }

    public static void prepareGetBankList(Map<String, String> params, String pCheckSumBankList) throws Exception {
        putBase(params);
        params.put(ConstantParams.PLATFORM, BuildConfig.PAYMENT_PLATFORM);
        params.put(ConstantParams.CHECKSUM, pCheckSumBankList != null ? pCheckSumBankList : "");
    }

    /**
     * Add params ATM AuthenPayerTask
     *
     * @param params
     * @param pTransID
     * @param pAuthenType
     * @param pAuthenValue
     */
    public static void prepareAtmAuthenPayer(Map<String, String> params, String pTransID, String pAuthenType, String pAuthenValue) throws Exception {
        putBase(params);
        params.put(ConstantParams.USER_ID, GlobalData.getPaymentInfo().userInfo.zaloPayUserId);
        params.put(ConstantParams.ACCESS_TOKEN, GlobalData.getPaymentInfo().userInfo.accessToken);
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


    public static boolean prepareSDKReport(Map<String, String> params, String pTranID, String pBankCode, int pExInfo, String pException) {
        try {
            putBase(params);
            params.put(ConstantParams.USER_ID, GlobalData.getPaymentInfo().userInfo.zaloPayUserId);
            params.put(ConstantParams.ACCESS_TOKEN, GlobalData.getPaymentInfo().userInfo.accessToken);
            params.put(ConstantParams.TRANSID, !TextUtils.isEmpty(pTranID) ? pTranID : "");
            params.put(ConstantParams.BANK_CODE, !TextUtils.isEmpty(pBankCode) ? pBankCode : "");
            params.put(ConstantParams.EXINFO, (pExInfo != SDKReportTask.DEFAULT) ? String.valueOf(pExInfo) : "");
            params.put(ConstantParams.EXCEPTION, !TextUtils.isEmpty(pException) ? pException : "");
            return true;
        } catch (Exception ex) {
            Log.d("prepareSDKReport", ex);
            return false;
        }
    }

    /**
     * Add params Get Status
     *
     * @param params
     * @param pTransID
     */
    public static void prepareGetStatusParams(Map<String, String> params, String pTransID) throws Exception {
        putBase(params);
        params.put(ConstantParams.APP_ID, String.valueOf(GlobalData.getPaymentInfo().appID));
        params.put(ConstantParams.ACCESS_TOKEN, GlobalData.getPaymentInfo().userInfo.accessToken);
        params.put(ConstantParams.ZP_TRANSID, pTransID);
        params.put(ConstantParams.USER_ID, GlobalData.getPaymentInfo().userInfo.zaloPayUserId);
        params.put(ConstantParams.DEVICE_ID, DeviceUtil.getUniqueDeviceID(GlobalData.getAppContext()));
    }

    /***
     * params for get transaction status in network error case
     *
     * @param params
     * @param pAppTransID
     */
    public static void prepareCheckSubmitOrderStatusParams(Map<String, String> params, String pAppTransID) throws Exception {
        putBase(params);
        params.put(ConstantParams.APP_ID, String.valueOf(GlobalData.getPaymentInfo().appID));
        params.put(ConstantParams.APP_TRANS_ID, pAppTransID);
        params.put(ConstantParams.USER_ID, GlobalData.getPaymentInfo().userInfo.zaloPayUserId);
    }

    /***
     * get card info params
     * @param params
     * @throws Exception
     */
    public static void prepareGetCardInfoListParams(Map<String, String> params, UserInfo pUserInfo) throws Exception {
        putBase(params);
        params.put(ConstantParams.ACCESS_TOKEN, pUserInfo.accessToken);
        params.put(ConstantParams.USER_ID, pUserInfo.zaloPayUserId);

        String checkSum = SharedPreferencesManager.getInstance().getCardInfoCheckSum();
        if (checkSum == null) {
            checkSum = "";
        }
        params.put(ConstantParams.CARDINFO_CHECKSUM, checkSum);
    }

    public static void prepareGetBankAccountListParams(Map<String, String> params) throws Exception {
        putBase(params);
        params.put(ConstantParams.ACCESS_TOKEN, GlobalData.getPaymentInfo().userInfo.accessToken);
        params.put(ConstantParams.USER_ID, GlobalData.getPaymentInfo().userInfo.zaloPayUserId);

        String checkSum = SharedPreferencesManager.getInstance().getBankAccountCheckSum();
        if (checkSum == null) {
            checkSum = "";
        }
        params.put(ConstantParams.BANKACCOUNT_CHECKSUM, checkSum);
    }

<<<<<<< HEAD
    /***
     * Add params get appinfo
     *
     * @param pZaloUserId
     * @param pAppId
     * @param pAccessToken
     * @param pCheckSum
     * @param params
     */
    public static void prepareGetAppInfoParams(String pZaloUserId, String pAppId, String pAccessToken, String pCheckSum, Map<String, String> params) {
=======
    public static void prepareGetAppInfoParams(String pZaloUserId, String pAppId, String pAccessToken, String pAppInfoCheckSum, int[] pTranstype, String[] pTranstypeCheckSum, HashMap<String, String> params) {
>>>>>>> c78224b... [SDK] Update app info v1
        putBase(params);
        params.put(ConstantParams.USER_ID, pZaloUserId);
        params.put(ConstantParams.APP_ID, pAppId);
        params.put(ConstantParams.ACCESS_TOKEN, pAccessToken);
        params.put(ConstantParams.APPINFO_CHECKSUM, !TextUtils.isEmpty(pAppInfoCheckSum) ? pAppInfoCheckSum : "");
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder stringBuilder1 = new StringBuilder();
        stringBuilder.append("[");
        for (int i = 0; i < pTranstype.length; i++) {
            stringBuilder.append(pTranstype[i]);
            if (i + 1 < pTranstype.length) {
                stringBuilder.append(",");
            }
        }
        stringBuilder.append("]");
        params.put(ConstantParams.TRANSTYPE, stringBuilder.toString());

        stringBuilder1.append("[");
        for (int i = 0; i < pTranstypeCheckSum.length; i++) {
            stringBuilder1.append(pTranstypeCheckSum[i] != null ? pTranstypeCheckSum[i] : "");
            if (i + 1 < pTranstypeCheckSum.length) {
                stringBuilder1.append(",");
            }
        }
        stringBuilder1.append("]");
        params.put(ConstantParams.TRANSTYPECHECKSUMS, stringBuilder1.toString());
    }

    /**
     * Add params get platformInfo
     *
     * @param checksum
     * @param resrcVer
     * @param params
     */
    public static void prepareGetPlatformInfoParams(String checksum, String resrcVer, String cardInfoCheckSum, String bankAccountChecksum, Map<String, String> params) throws Exception {

        ZPWPaymentInfo paymentInfo = GlobalData.getPaymentInfo();

        cardInfoCheckSum = cardInfoCheckSum != null ? cardInfoCheckSum : "";
        checksum = checksum != null ? checksum : "";
        resrcVer = resrcVer != null ? resrcVer : "";
        bankAccountChecksum = bankAccountChecksum != null ? bankAccountChecksum : "";

        params.put(ConstantParams.USER_ID, paymentInfo.userInfo.zaloPayUserId);
        params.put(ConstantParams.ACCESS_TOKEN, paymentInfo.userInfo.accessToken);
        params.put(ConstantParams.PLATFORM_CODE, BuildConfig.PAYMENT_PLATFORM);
        params.put(ConstantParams.DS_SCREEN_TYPE, DimensionUtil.getScreenType(GlobalData.getAppContext()));
        params.put(ConstantParams.PLATFORM_IN_FOCHECKSUM, checksum);
        params.put(ConstantParams.RESOURCE_VERSION, resrcVer);
        params.put(ConstantParams.APP_VERSION, SdkUtils.getAppVersion(GlobalData.getAppContext()));
        params.put(ConstantParams.DEVICE_MODEL, DeviceUtil.getDeviceName());
        params.put(ConstantParams.MNO, ConnectionUtil.getSimOperator(GlobalData.getAppContext()));
        params.put(ConstantParams.CARDINFO_CHECKSUM, cardInfoCheckSum);
        params.put(ConstantParams.BANKACCOUNT_CHECKSUM, bankAccountChecksum);

    }

    public static void prepareGetStatusMapCardParams(Map<String, String> params, String pTransID) throws Exception {
        putBase(params);
        params.put(ConstantParams.ACCESS_TOKEN, GlobalData.getPaymentInfo().userInfo.accessToken);
        params.put(ConstantParams.ZP_TRANSID, pTransID);
        params.put(ConstantParams.USER_ID, GlobalData.getPaymentInfo().userInfo.zaloPayUserId);
    }

    public static void prepareVerifyMapCardParams(AdapterBase pAdapter, Map<String, String> params) throws Exception {
        params.put(ConstantParams.ACCESS_TOKEN, GlobalData.getPaymentInfo().userInfo.accessToken);
        params.put(ConstantParams.USER_ID, GlobalData.getPaymentInfo().userInfo.zaloPayUserId);
        params.put(ConstantParams.ZALO_ID, GlobalData.getPaymentInfo().userInfo.zaloUserId);
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

    /**
     * Add params submit transaction
     *
     * @param pAdapter
     * @param pmcID
     * @param params
     */
    public static boolean prepareSubmitTransactionParams(AdapterBase pAdapter, String pmcID, Map<String, String> params) throws Exception {
        putBaseParameter(params);
<<<<<<< HEAD
        if (pAdapter != null) {
            params.put(ConstantParams.PMC_ID, pAdapter.getChannelID());
        }
        if (!TextUtils.isEmpty(pmcID)) {
=======

        if (pAdapter != null)
            params.put(ConstantParams.PMC_ID, String.valueOf(pAdapter.getChannelID()));

        if (!TextUtils.isEmpty(pmcID))
>>>>>>> 9fd9a35... [SDK] Apply app info v1
            params.put(ConstantParams.PMC_ID, pmcID);
        }
        if (GlobalData.isMapCardChannel() && GlobalData.getPaymentInfo().mapBank.isValid()) {
            DMappedCreditCard mapCard = new DMappedCreditCard((DMappedCard) GlobalData.getPaymentInfo().mapBank);
            params.put(ConstantParams.CHARGE_INFO, GsonUtils.toJsonString(mapCard));
        } else if (GlobalData.isMapBankAccountChannel() && GlobalData.getPaymentInfo().mapBank.isValid()) {
            params.put(ConstantParams.CHARGE_INFO, GsonUtils.toJsonString(GlobalData.getPaymentInfo().mapBank));
        } else if (pAdapter != null && pAdapter.isCardFlow()) {
            params.put(ConstantParams.CHARGE_INFO, GsonUtils.toJsonString(pAdapter.getCard()));
        } else {
            params.put(ConstantParams.CHARGE_INFO, "");
        }
        if (!TextUtils.isEmpty(GlobalData.getTransactionPin())) {
            params.put(ConstantParams.PIN, GlobalData.getTransactionPin());
        }
        params.put(ConstantParams.TRANS_TYPE, String.valueOf(GlobalData.getTransactionType()));
        params.put(ConstantParams.ACCESS_TOKEN, GlobalData.getPaymentInfo().userInfo.accessToken);
        params.put(ConstantParams.USER_ID, GlobalData.getPaymentInfo().userInfo.zaloPayUserId);
        params.put(ConstantParams.ZALO_ID, GlobalData.getPaymentInfo().userInfo.zaloUserId);
        double lat = 0, lng = 0;
        if (GlobalData.getPaymentInfo().mLocation != null) {
            lat = GlobalData.getPaymentInfo().mLocation.latitude;
            lng = GlobalData.getPaymentInfo().mLocation.longitude;
        }
        params.put(ConstantParams.LATTITUDE, String.valueOf(lat));
        params.put(ConstantParams.LONGITUDE, String.valueOf(lng));
        return true;
    }

    /**
     * Add params Base
     *
     * @param params
     */
    public static void putBaseParameter(Map<String, String> params) throws Exception {
        ZPWPaymentInfo paymentInfo = GlobalData.getPaymentInfo();
        params.put(ConstantParams.APP_ID, String.valueOf(paymentInfo.appID));
        params.put(ConstantParams.APP_TRANS_ID, paymentInfo.appTransID);
        params.put(ConstantParams.APP_USER, paymentInfo.appUser);
        params.put(ConstantParams.APP_TIME, String.valueOf(paymentInfo.appTime));
        params.put(ConstantParams.ITEM, paymentInfo.itemName);
        params.put(ConstantParams.DESCRIPTION, paymentInfo.description);
        params.put(ConstantParams.EMBED_DATA, paymentInfo.embedData);
        params.put(ConstantParams.MAC, paymentInfo.mac);
        params.put(ConstantParams.PLATFORM, BuildConfig.PAYMENT_PLATFORM);
        params.put(ConstantParams.PLATFORM_CODE, BuildConfig.PAYMENT_PLATFORM);
        params.put(ConstantParams.AMOUNT, String.valueOf(GlobalData.getPaymentInfo().amount));
        params.put(ConstantParams.DEVICE_ID, DeviceUtil.getUniqueDeviceID(GlobalData.getAppContext()));
        params.put(ConstantParams.DEVICE_MODEL, DeviceUtil.getDeviceName());
        params.put(ConstantParams.APP_VERSION, SdkUtils.getAppVersion(GlobalData.getAppContext()));
        params.put(ConstantParams.SDK_VERSION, BuildConfig.SDK_BUILD_VERSION);
        params.put(ConstantParams.OS_VERSION, Build.VERSION.RELEASE);
        params.put(ConstantParams.CONN_TYPE, ConnectionUtil.getConnectionType(GlobalData.getAppContext()));
        params.put(ConstantParams.MNO, ConnectionUtil.getSimOperator(GlobalData.getAppContext()));
    }

    public static void putBase(Map<String, String> params) {
        params.put(ConstantParams.APP_VERSION, SdkUtils.getAppVersion(GlobalData.getAppContext()));
    }

    /**
     * Add params Base
     *
     * @param params
     */
    public static void prepareMapAccountParams(Map<String, String> params, String pBankAccInfo) throws Exception {
        ZPWPaymentInfo paymentInfo = GlobalData.getPaymentInfo();
        params.put(ConstantParams.USER_ID, String.valueOf(paymentInfo.userInfo.zaloPayUserId));
        params.put(ConstantParams.ZALO_ID, String.valueOf(paymentInfo.userInfo.zaloUserId));
        params.put(ConstantParams.ACCESS_TOKEN, String.valueOf(paymentInfo.userInfo.accessToken));
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

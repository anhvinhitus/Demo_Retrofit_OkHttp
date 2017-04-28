package vn.com.zalopay.wallet.business.data;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import java.lang.ref.WeakReference;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DAppInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPaymentChannelView;
import vn.com.zalopay.wallet.business.entity.user.ListUserProfile;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.entity.user.UserProfile;
import vn.com.zalopay.wallet.business.fingerprint.IPaymentFingerPrint;
import vn.com.zalopay.wallet.business.fingerprint.PaymentFingerPrint;
import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.constants.CardChannel;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.controller.SDKPayment;
import vn.com.zalopay.wallet.listener.IChannelActivityCallBack;
import vn.com.zalopay.wallet.listener.ZPPaymentListener;
import vn.com.zalopay.wallet.listener.ZPWGatewayInfoCallback;
import vn.com.zalopay.wallet.utils.ConnectionUtil;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.view.component.activity.BasePaymentActivity;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

/***
 * static class contain static data.
 * need to dispose everything after quit sdk
 */
public class GlobalData {
    //region static variable

    public static long appID = 1;
    //static amount when user select 1 channel for paying.
    public static double orderAmountTotal = 0;
    public static double orderAmountFee = 0;
    @CardChannel
    public static int cardChannelType = CardChannel.ATM;
    //callback to merchant after sdk retry load gateway info
    protected static WeakReference<ZPWGatewayInfoCallback> mMerchantCallBack;
    @BankFunctionCode
    private static int bankFunction = BankFunctionCode.PAY;
    @TransactionType
    private static int transactionType = TransactionType.PAY;

    //app's activity is calling sdk.
    private static WeakReference<Activity> mMerchantActivity = null;
    private static ZPPaymentListener mListener = null;
    private static ZPWPaymentInfo mPaymentInfo = null;
    private static ZPPaymentResult paymentResult = null;
    /***
     * user level,map table
     * list contain policy for allowing pay which channel by which level.
     */
    private static ListUserProfile mUserProfile = null;
    //pin for payment
    private static String mTransactionPin = null;
    private static WeakReference<IChannelActivityCallBack> mChannelActivityCallBack;

    public static ZPWGatewayInfoCallback getMerchantCallBack() {
        if (mMerchantCallBack != null)
            return mMerchantCallBack.get();
        return null;
    }

    public static void setMerchantCallBack(ZPWGatewayInfoCallback pMerchantCallBack) {
        GlobalData.mMerchantCallBack = new WeakReference<>(pMerchantCallBack);
    }

    public static IChannelActivityCallBack getChannelActivityCallBack() {
        if (mChannelActivityCallBack != null) {
            return mChannelActivityCallBack.get();
        }
        return null;
    }

    public static void setChannelActivityCallBack(IChannelActivityCallBack mChannelActivityCallBack) {
        GlobalData.mChannelActivityCallBack = new WeakReference<>(mChannelActivityCallBack);
    }

    //endregion

    //region functions

    /***
     * prevent cross call pay
     */
    private static boolean isAccessRight() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        boolean isRightAccess = false;

        if (stackTraceElements.length >= 5) {
            if (stackTraceElements[4].getClassName().equals(SDKPayment.class.getName()) && stackTraceElements[4].getMethodName().equals("pay")) {
                isRightAccess = true;
            }
        }

        return isRightAccess;
    }

    public static boolean isUserInSDK() {
        return BasePaymentActivity.getCurrentActivityCount() > 0;
    }

    public static boolean isEnoughMoneyForTransaction(long pFee) {
        return GlobalData.getPaymentInfo() != null && GlobalData.getPaymentInfo().userInfo != null && GlobalData.getPaymentInfo().userInfo.balance >= (GlobalData.getOrderAmount() + pFee);
    }

    public static boolean isNewUser() {
        String userID;
        try {
            userID = SharedPreferencesManager.getInstance().getCurrentUserID();

        } catch (Exception e) {
            Log.e("isNewUser", e);

            return true;
        }

        return TextUtils.isEmpty(userID) || !userID.equals(GlobalData.mPaymentInfo.userInfo.zaloPayUserId);

    }

    /***
     * check soft token
     * if has new accesstoken, must notify to app to update new token to cache again
     * @param pResponse
     * @return
     */
    public static boolean checkForUpdateAccessTokenToApp(BaseResponse pResponse) {
        if (pResponse == null || TextUtils.isEmpty(pResponse.accesstoken)) {
            Log.d("checkForUpdateAccessTokenToApp", "===pResponse=NULL || accesstoken=NULL");
            return false;
        }

        if (GlobalData.mPaymentInfo == null || GlobalData.mPaymentInfo.userInfo == null) {
            Log.d("checkForUpdateAccessTokenToApp", "paymentInfo=NULL");
            return false;
        }

        Log.d("checkForUpdateAccessTokenToApp", "old token = " + GlobalData.mPaymentInfo.userInfo.accessToken);
        Log.d("checkForUpdateAccessTokenToApp", "new token = " + pResponse.accesstoken);
        if (GlobalData.getPaymentListener() != null && !TextUtils.isEmpty(GlobalData.mPaymentInfo.userInfo.accessToken) && !GlobalData.mPaymentInfo.userInfo.accessToken.equals(pResponse.accesstoken)) {
            //we need to callback to app to update new access token
            GlobalData.getPaymentListener().onUpdateAccessToken(pResponse.accesstoken);
            GlobalData.mPaymentInfo.userInfo.accessToken = pResponse.accesstoken;
            return true;
        }
        return false;
    }

    //region is channel

    /***
     * for checking user selected a map card channel.
     */
    public static boolean isMapCardChannel() {
        return getPaymentInfo() != null && getPaymentInfo().mapBank instanceof DMappedCard && !TextUtils.isEmpty(getPaymentInfo().mapBank.getFirstNumber())
                && !TextUtils.isEmpty(getPaymentInfo().mapBank.getLastNumber());

    }

    public static boolean isMapBankAccountChannel() {
        return getPaymentInfo() != null && getPaymentInfo().mapBank instanceof DBankAccount && !TextUtils.isEmpty(getPaymentInfo().mapBank.getFirstNumber())
                && !TextUtils.isEmpty(getPaymentInfo().mapBank.getLastNumber());

    }

    public static boolean isBankAccountLink() {
        return transactionType == TransactionType.LINK_ACCOUNT;
    }

    public static boolean isLinkAccFlow() {
        return getPaymentInfo() != null && getPaymentInfo().linkAccInfo != null && getPaymentInfo().linkAccInfo.isLinkAcc();
    }

    public static boolean isUnLinkAccFlow() {
        return getPaymentInfo() != null && getPaymentInfo().linkAccInfo != null && getPaymentInfo().linkAccInfo.isUnlinkAcc();
    }

    public static boolean isLinkCardChannel() {
        return transactionType == TransactionType.LINK_CARD;
    }

    public static boolean isTranferMoneyChannel() {
        return transactionType == TransactionType.MONEY_TRANSFER;
    }

    public static boolean isTopupChannel() {
        return transactionType == TransactionType.TOPUP;
    }

    public static boolean isWithDrawChannel() {
        return transactionType == TransactionType.WITHDRAW;
    }

    public static boolean isPayChannel() {
        return transactionType == TransactionType.PAY;
    }

    public static boolean isChannelHasInputCard() {
        boolean isTransactionHasInputCard = !GlobalData.isMapCardChannel() && !GlobalData.isMapBankAccountChannel() && !GlobalData.isWithDrawChannel();

        boolean isChannelHasInputCard = true;

        if (BasePaymentActivity.getCurrentActivity() instanceof PaymentChannelActivity
                && ((PaymentChannelActivity) BasePaymentActivity.getCurrentActivity()).getAdapter().isZaloPayFlow()) {
            isChannelHasInputCard = false;
        }
        return isTransactionHasInputCard && isChannelHasInputCard;
    }
    //endregion

    public static String getOfflineMessage() {
        if (GlobalData.isLinkAccFlow()) {
            return GlobalData.getStringResource(RS.string.sdk_alert_networking_off_in_link_account);
        } else if (GlobalData.isUnLinkAccFlow()) {
            return GlobalData.getStringResource(RS.string.sdk_alert_networking_off_in_unlink_account);
        } else {
            return GlobalData.getStringResource(RS.string.zpw_alert_networking_off_in_transaction);
        }
    }

    public static boolean updateResultNetworkingError(String pMessage) {
        boolean isOffNetworking;

        try {
            isOffNetworking = !ConnectionUtil.isOnline(BasePaymentActivity.getCurrentActivity());
        } catch (Exception ex) {
            Log.e("updateResultNetworkingError", ex);

            isOffNetworking = false;
        }

        if (isOffNetworking &&
                (pMessage.equals(GlobalData.getStringResource(RS.string.zingpaysdk_alert_no_connection)) ||
                        pMessage.equals(GlobalData.getStringResource(RS.string.zpw_alert_networking_off_in_transaction)) ||
                        pMessage.equals(GlobalData.getStringResource(RS.string.sdk_alert_networking_off_in_link_account)) ||
                        pMessage.equals(GlobalData.getStringResource(RS.string.sdk_alert_networking_off_in_unlink_account)))) {
            setResultNoInternet();
        }

        return isOffNetworking;
    }

    //region set transaction result to notify to app
    public static void setResult(@PaymentStatus int pStatus) {
        GlobalData.getPaymentResult().paymentStatus = pStatus;
    }

    public static void setResultSuccess() {
        GlobalData.getPaymentResult().paymentStatus = PaymentStatus.ZPC_TRANXSTATUS_SUCCESS;
    }

    public static void setResultFail() {
        GlobalData.getPaymentResult().paymentStatus = PaymentStatus.ZPC_TRANXSTATUS_FAIL;
    }

    public static void setResultMoneyNotEnough() {
        GlobalData.getPaymentResult().paymentStatus = PaymentStatus.ZPC_TRANXSTATUS_MONEY_NOT_ENOUGH;
    }

    public static void setResultProcessing() {
        GlobalData.getPaymentResult().paymentStatus = PaymentStatus.ZPC_TRANXSTATUS_PROCESSING;
    }

    public static void setResultUpgrade() {
        GlobalData.getPaymentResult().paymentStatus = PaymentStatus.ZPC_TRANXSTATUS_UPGRADE;
    }

    public static void setResultUpgradeCMND() {
        GlobalData.getPaymentResult().paymentStatus = PaymentStatus.ZPC_TRANXSTATUS_UPGRADE_CMND_EMAIL;
    }

    public static void setResultInvalidInput() {
        GlobalData.getPaymentResult().paymentStatus = PaymentStatus.ZPC_TRANXSTATUS_INPUT_INVALID;
    }

    public static void setResultInvalidToken() {
        GlobalData.getPaymentResult().paymentStatus = PaymentStatus.ZPC_TRANXSTATUS_TOKEN_INVALID;
    }

    public static void setResultLockUser() {
        GlobalData.getPaymentResult().paymentStatus = PaymentStatus.ZPC_TRANXSTATUS_LOCK_USER;
    }

    public static void setResultServiceMaintenance() {
        GlobalData.getPaymentResult().paymentStatus = PaymentStatus.ZPC_TRANXSTATUS_SERVICE_MAINTENANCE;
    }

    public static void setResultUserClose() {
        GlobalData.getPaymentResult().paymentStatus = PaymentStatus.ZPC_TRANXSTATUS_CLOSE;
    }

    public static void setResultNeedToLinkCard() {
        GlobalData.getPaymentResult().paymentStatus = PaymentStatus.ZPC_TRANXSTATUS_NEED_LINKCARD;
    }

    public static void setResultNoInternet() {
        GlobalData.getPaymentResult().paymentStatus = PaymentStatus.ZPC_TRANXSTATUS_NO_INTERNET;
    }

    public static void setResultNeedToLinkAccount() {
        GlobalData.getPaymentResult().paymentStatus = PaymentStatus.ZPC_TRANXSTATUS_NEED_LINK_ACCOUNT;
    }

    public static void setResultNeedToLinkAccountBeforePayment() {
        GlobalData.getPaymentResult().paymentStatus = PaymentStatus.ZPC_TRANXSTATUS_NEED_LINK_ACCOUNT_BEFORE_PAYMENT;
    }

    public static void setResultUpLevelLinkAccountAndPayment() {
        GlobalData.getPaymentResult().paymentStatus = PaymentStatus.ZPC_TRANXSTATUS_UPLEVEL_AND_LINK_BANKACCOUNT_CONTINUE_PAYMENT;
    }

    public static void setResultNeedToLinkCardBeforePayment() {
        GlobalData.getPaymentResult().paymentStatus = PaymentStatus.ZPC_TRANXSTATUS_NEED_LINKCARD_BEFORE_PAYMENT;
    }
    //endregion

    /***
     * is this app configured by backend.
     */
    public static boolean isAllowApplication() {
        try {
            DAppInfo currentApp = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getAppById(String.valueOf(appID)), DAppInfo.class);

            return (currentApp != null && currentApp.isAllow());

        } catch (Exception e) {
            Log.e("isAllowApplication", e);
        }
        return false;
    }

    /***
     * this is red package channel
     */
    public static boolean isRedPacketChannel() {
        long redPackageID;

        try {
            redPackageID = Long.parseLong(GlobalData.getStringResource(RS.string.zpw_redpackage_app_id));

            return redPackageID == GlobalData.appID;
        } catch (Exception ignored) {
        }
        return false;

    }

    /***
     * this is zalopay channel.
     */
    public static boolean isZalopayChannel() {
        try {
            long zaloPayID = Long.parseLong(RS.string.zingpaysdk_conf_gwinfo_channel_zalopay);

            return zaloPayID == GlobalData.appID;
        } catch (Exception ignored) {

        }
        return false;
    }

    public static void selectBankFunctionByTransactionType() {
        switch (transactionType) {
            case TransactionType.LINK_ACCOUNT:
                bankFunction = BankFunctionCode.LINK_BANK_ACCOUNT;
                break;
            case TransactionType.LINK_CARD:
                bankFunction = BankFunctionCode.LINK_CARD;
                break;
            case TransactionType.MONEY_TRANSFER:
                bankFunction = BankFunctionCode.PAY;
                break;
            case TransactionType.WITHDRAW:
                bankFunction = BankFunctionCode.WITHDRAW;
                break;
            case TransactionType.TOPUP:
                bankFunction = BankFunctionCode.PAY;
                break;
            case TransactionType.PAY:
                bankFunction = BankFunctionCode.PAY;
        }
    }

    /***
     * Get transtype of payment.
     */
    @TransactionType
    public static int getTransactionType() {
        if (transactionType == TransactionType.PAY && (GlobalData.appID == Long.parseLong(getStringResource(RS.string.zpw_conf_wallet_id)))) {
            transactionType = TransactionType.TOPUP;
        }
        return transactionType;
    }

    @BankFunctionCode
    public static int getPayBankFunction(DPaymentChannelView pChannel) {
        if (pChannel.isBankAccountMap()) {
            bankFunction = BankFunctionCode.PAY_BY_BANKACCOUNT_TOKEN;
        } else if (pChannel.isMapCardChannel()) {
            bankFunction = BankFunctionCode.PAY_BY_CARD_TOKEN;
        } else if (pChannel.isBankAccount()) {
            bankFunction = BankFunctionCode.PAY_BY_BANK_ACCOUNT;
        } else {
            bankFunction = BankFunctionCode.PAY_BY_CARD;
        }

        return bankFunction;
    }

    @BankFunctionCode
    public static int getBankFunctionPay() {
        if (GlobalData.isMapBankAccountChannel()) {
            bankFunction = BankFunctionCode.PAY_BY_BANKACCOUNT_TOKEN;
        } else if (GlobalData.isMapCardChannel()) {
            bankFunction = BankFunctionCode.PAY_BY_CARD_TOKEN;
        } else {
            bankFunction = BankFunctionCode.PAY_BY_CARD;
        }
        return bankFunction;
    }

    public static void initApplication(ZPWPaymentInfo pPaymentInfo) {
        GlobalData.mPaymentInfo = pPaymentInfo;
        initResultReturn();
    }

    public static void initApplicationUserInfo(ZPWPaymentInfo pPaymentInfo) {
        if (GlobalData.mPaymentInfo != null && GlobalData.mPaymentInfo.userInfo != null) {
            GlobalData.mPaymentInfo.userInfo.zaloPayUserId = pPaymentInfo.userInfo.zaloPayUserId;
            GlobalData.mPaymentInfo.userInfo.accessToken = pPaymentInfo.userInfo.accessToken;
        } else {
            GlobalData.mPaymentInfo = pPaymentInfo;
        }
    }

    public static void setUserInfo(UserInfo pUserInfo) {
        Log.d("GlobalData", "setUserInfo=" + GsonUtils.toJsonString(pUserInfo));
        if (GlobalData.mPaymentInfo == null) {
            GlobalData.mPaymentInfo = new ZPWPaymentInfo();
        }

        if (GlobalData.mPaymentInfo.userInfo == null) {
            GlobalData.mPaymentInfo.userInfo = new UserInfo();
        }
        if (pUserInfo != null) {
            GlobalData.mPaymentInfo.userInfo.zaloPayUserId = pUserInfo.zaloPayUserId;
            GlobalData.mPaymentInfo.userInfo.accessToken = pUserInfo.accessToken;
        }
    }

    //endregion

    public static void initResultReturn() {
        GlobalData.setResultFail();
    }

    /***
     * alwaw call this to set static listener and info.
     */
    public static void setSDKData(Activity pActivity, ZPPaymentListener pPaymentListener, @TransactionType int pTransactionType) throws Exception {
        if (!isAccessRight()) {
            throw new Exception("Violate Design Pattern! Only 'pay' static method of ZingPayService class can set application!");
        }
        PaymentSessionInfo paymentSessionInfo = PaymentSessionInfo.shared();
        GlobalData.mPaymentInfo = paymentSessionInfo.getPaymentInfo();
        GlobalData.appID = GlobalData.mPaymentInfo.appID;
        GlobalData.mMerchantActivity = new WeakReference<>(pActivity);
        GlobalData.mListener = pPaymentListener;
        GlobalData.transactionType = pTransactionType;

        //reset data
        GlobalData.paymentResult = null;
        GlobalData.mTransactionPin = null;
        GlobalData.mUserProfile = null;
        GlobalData.orderAmountTotal = GlobalData.mPaymentInfo.amount;
        GlobalData.orderAmountFee = 0;
        AdapterBase.existedMapCard = false;

        initResultReturn();

    }

    public static void setIFingerPrint(IPaymentFingerPrint pFingerPrintFromMerchant) {
        PaymentFingerPrint.shared().setPaymentFingerPrint(pFingerPrintFromMerchant);
    }

    public static void terminateSDK() {
        Activity currentActivity = BasePaymentActivity.getCurrentActivity();
        if (currentActivity instanceof BasePaymentActivity) {
            ((BasePaymentActivity) currentActivity).onExit(currentActivity.getString(R.string.zingpaysdk_alert_context_error), true);
        }
    }

    public static Context getAppContext() {
        try {
            return SDKApplication.getApplication();
        } catch (Exception e) {
            Log.e(GlobalData.class.getName(), e);

            terminateSDK();
        }

        return null;
    }

    public static Activity getMerchantActivity() {
        if (mMerchantActivity != null)
            return mMerchantActivity.get();

        return null;
    }

    public static String getTransactionPin() {
        return mTransactionPin;
    }

    public static void setTransactionPin(String mTransactionPin) {
        GlobalData.mTransactionPin = mTransactionPin;
    }

    /***
     * return user balance
     */
    public static long getBalance() {
        if (getPaymentInfo() != null && getPaymentInfo().userInfo != null)
            return getPaymentInfo().userInfo.balance;

        return 0;
    }

    /***
     * return user level.
     */
    public static int getLevel() {
        if (getPaymentInfo() != null && getPaymentInfo().userInfo != null)
            return getPaymentInfo().userInfo.level;

        return 0;
    }

    public static ZPPaymentListener getPaymentListener() {
        return mListener;
    }

    public static ZPWPaymentInfo getPaymentInfo() {
        if (GlobalData.mPaymentInfo == null) {
            if (GlobalData.getPaymentResult() != null) {
                GlobalData.setResultInvalidInput();
            }
            if (BasePaymentActivity.getCurrentActivity() instanceof BasePaymentActivity) {
                ((BasePaymentActivity) BasePaymentActivity.getCurrentActivity()).recycleActivity();
            }
            Log.e("getPaymentInfo", "get payment info is null");
            return null;
        }
        return GlobalData.mPaymentInfo;
    }

    public static boolean isForceChannel() {
        return getPaymentInfo() != null && getPaymentInfo().isForceChannel();
    }

    public static long getOrderAmount() {
        if (getPaymentInfo() != null)
            return getPaymentInfo().amount;

        return 0;
    }

    public static ZPPaymentResult getPaymentResult() {
        if (GlobalData.paymentResult == null) {
            GlobalData.paymentResult = new ZPPaymentResult(mPaymentInfo, PaymentStatus.ZPC_TRANXSTATUS_FAIL);
        }

        return GlobalData.paymentResult;
    }

    public static String getTransProcessingMessage() {
        return isLinkCardChannel() ? RS.string.zingpaysdk_alert_processing_get_status_linkcard_fail : RS.string.zingpaysdk_alert_processing_get_status_fail;
    }

    public static String getStringResource(String pResourceID) {

        try {
            // Try to get string from resource sent from before get from local
            String result = ResourceManager.getInstance(null).getString(pResourceID);

            return (result != null) ? result : getAppContext().getString(RS.getString(pResourceID));

        } catch (Exception e) {
            Log.e(GlobalData.class.getName(), e);
        }

        return null;
    }

    /***
     * load user table map into variable static
     */
    private static void loadPermissionLevelMap() {
        if (getPaymentInfo().userInfo != null && !TextUtils.isEmpty(getPaymentInfo().userInfo.userProfile) && getUserProfileList() == null) {
            try {
                GlobalData.mUserProfile = GsonUtils.fromJsonString(getPaymentInfo().userInfo.userProfile, ListUserProfile.class);

                //for testing
                /*
                UserProfile vcb = new UserProfile();
                vcb.allow = true;
                vcb.pmcid = 37;
                vcb.transtype = 1;
                vcb.requireotp = false;
                GlobalData.mUserProfile.profilelevelpermisssion.add(vcb);
                */
                /*
                for (int i=0;i<GlobalData.mUserProfile.profilelevelpermisssion.size();i++)
				{
					if(GlobalData.mUserProfile.profilelevelpermisssion.get(i).transtype == 1 && (GlobalData.mUserProfile.profilelevelpermisssion.get(i).pmcid == 37
                    ||GlobalData.mUserProfile.profilelevelpermisssion.get(i).pmcid == 38))
					{
						GlobalData.mUserProfile.profilelevelpermisssion.get(i).allow = false;
					}
				}
				*/


            } catch (Exception e) {
                Log.e("loadPermissionLevelMap", e);
            }
        }
    }

    /***
     * check whether user is allowed payment this channel.
     */
    public static int checkPermissionByChannelMap(int pChannelID) {
        loadPermissionLevelMap();
        UserProfile userProfile = GlobalData.getUserProfileAtChannel(pChannelID);

        if (userProfile == null)
            return Constants.LEVELMAP_INVALID;

        return userProfile.allow ? Constants.LEVELMAP_ALLOW : Constants.LEVELMAP_BAN;
    }

    /***
     * check whether creditcard channel require otp?
     */
    public static int isRequireOtpCreditCard() {
        BankConfig bankConfig;
        try {
            bankConfig = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getBankConfig(Constants.CCCode), BankConfig.class);


            if (bankConfig == null)
                return Constants.INPUT_INVALID;

            if (bankConfig != null && bankConfig.isRequireOtp())
                return Constants.REQUIRE_OTP;

            return Constants.REQUIRE_PIN;

        } catch (Exception e) {
            Log.e("isRequireOtpCreditCard", e);
        }

        return Constants.INPUT_INVALID;
    }

    public static UserProfile getUserProfileAtChannel(int pPmcID) {
        if (getUserProfileList() == null) {
            Log.d("===UserProfile===", "is null");

            return null;
        }

        try {
            Log.d("===UserProfile=====", GsonUtils.toJsonString(getUserProfileList()));

            for (int i = 0; i < getUserProfileList().profilelevelpermisssion.size(); i++) {
                if (getUserProfileList().profilelevelpermisssion.get(i).pmcid == pPmcID
                        && getUserProfileList().profilelevelpermisssion.get(i).transtype == transactionType) {
                    return getUserProfileList().profilelevelpermisssion.get(i);
                }
            }
        } catch (Exception ex) {
            Log.e("===getUserProfileAtChannel===", ex);
        }

        return null;
    }

    public static ListUserProfile getUserProfileList() {
        return mUserProfile;
    }

    public static boolean shouldNativeWebFlow() {
        /*try {
            BankConfig bankConfig = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getBankConfig(GlobalData.getPaymentInfo().linkAccInfo.getBankCode()), BankConfig.class);
            if (bankConfig != null && !bankConfig.isCoverBank()) {
                return true;
            }
        } catch (Exception e) {
            Log.e("shouldNativeWebFlow",e);
        }
        return false;*/
        return GlobalData.getStringResource(RS.string.sdk_vcb_flow_type).equals("1");
    }

    @BankFunctionCode
    public static int getCurrentBankFunction() {
        return bankFunction;
    }

    public static void setCurrentBankFunction(@BankFunctionCode int pBankFunction) {
        bankFunction = pBankFunction;
    }
    //endregion
}

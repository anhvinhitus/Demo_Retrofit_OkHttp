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
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentOption;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.base.ZPWNotification;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.business.entity.enumeration.EBankFunction;
import vn.com.zalopay.wallet.business.entity.enumeration.ECardChannelType;
import vn.com.zalopay.wallet.business.entity.enumeration.EPaymentStatus;
import vn.com.zalopay.wallet.business.entity.enumeration.ETransactionType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DAppInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPaymentChannelView;
import vn.com.zalopay.wallet.business.entity.user.ListUserProfile;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.entity.user.UserProfile;
import vn.com.zalopay.wallet.business.fingerprint.IPaymentFingerPrint;
import vn.com.zalopay.wallet.business.fingerprint.PaymentFingerPrint;
import vn.com.zalopay.wallet.controller.WalletSDKApplication;
import vn.com.zalopay.wallet.controller.WalletSDKPayment;
import vn.com.zalopay.wallet.listener.IChannelActivityCallBack;
import vn.com.zalopay.wallet.listener.ZPPaymentListener;
import vn.com.zalopay.wallet.listener.ZPWGatewayInfoCallback;
import vn.com.zalopay.wallet.listener.ZPWSaveMapCardListener;
import vn.com.zalopay.wallet.utils.ConnectionUtil;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;
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
    public static ECardChannelType cardChannelType = ECardChannelType.ATM;
    //save card listener for merchant
    public static ZPWSaveMapCardListener saveMapCardListener;
    //callback to merchant after sdk retry load gateway info
    protected static WeakReference<ZPWGatewayInfoCallback> mMerchantCallBack;
    //app's activity is calling sdk.
    private static WeakReference<Activity> mMerchantActivity = null;
    private static ZPPaymentListener mListener = null;
    private static ZPWPaymentInfo mPaymentInfo = null;
    private static ZPPaymentOption mPaymentOption = null;
    private static ETransactionType transactionType = ETransactionType.PAY;
    private static EBankFunction bankFunction = EBankFunction.PAY;
    private static ZPPaymentResult paymentResult = null;
    private static ZPWNotification mNotification;
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
        GlobalData.mMerchantCallBack = new WeakReference<ZPWGatewayInfoCallback>(pMerchantCallBack);
    }

    public static IChannelActivityCallBack getChannelActivityCallBack() {
        if (mChannelActivityCallBack != null) {
            return mChannelActivityCallBack.get();
        }
        return null;
    }

    public static void setChannelActivityCallBack(IChannelActivityCallBack mChannelActivityCallBack) {
        GlobalData.mChannelActivityCallBack = new WeakReference<IChannelActivityCallBack>(mChannelActivityCallBack);
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
            if (stackTraceElements[4].getClassName().equals(WalletSDKPayment.class.getName()) && stackTraceElements[4].getMethodName().equals("pay")) {
                isRightAccess = true;
            }
        }

        return isRightAccess;
    }

    public static boolean isUserInSDK() {
        return BasePaymentActivity.getCurrentActivityCount() > 0 ? true : false;
    }

    public static boolean isEnoughMoneyForTransaction(long pFee) {
        if (GlobalData.getPaymentInfo() != null && GlobalData.getPaymentInfo().userInfo != null) {
            return GlobalData.getPaymentInfo().userInfo.balance >= (GlobalData.getOrderAmount() + pFee);
        }
        return false;
    }

    public static boolean isNewUser() {
        String userID;
        try {
            userID = SharedPreferencesManager.getInstance().getCurrentUserID();

        } catch (Exception e) {
            Log.e("isNewUser", e);

            return true;
        }

        if (TextUtils.isEmpty(userID) || !userID.equals(GlobalData.mPaymentInfo.userInfo.zaloPayUserId)) {
            return true;
        }

        return false;
    }

    /***
     * check soft token
     * if has new accesstoken, must notify to app to update new token to cache again
     *
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
        if (getPaymentInfo() != null && getPaymentInfo().mapBank instanceof DMappedCard && !TextUtils.isEmpty(getPaymentInfo().mapBank.getFirstNumber())
                && !TextUtils.isEmpty(getPaymentInfo().mapBank.getLastNumber()))
            return true;

        return false;
    }

    public static boolean isMapBankAccountChannel() {
        if (getPaymentInfo() != null && getPaymentInfo().mapBank instanceof DBankAccount && !TextUtils.isEmpty(getPaymentInfo().mapBank.getFirstNumber())
                && !TextUtils.isEmpty(getPaymentInfo().mapBank.getLastNumber()))
            return true;

        return false;
    }

    public static boolean isLinkAccChannel() {
        return transactionType == ETransactionType.LINK_ACC;
    }

    public static boolean isLinkAccFlow() {
        return getPaymentInfo().linkAccInfo != null && getPaymentInfo().linkAccInfo.isLinkAcc();
    }

    public static boolean isUnLinkAccFlow() {
        return getPaymentInfo().linkAccInfo != null && getPaymentInfo().linkAccInfo.isUnlinkAcc();
    }

    public static boolean isLinkCardChannel() {
        return transactionType == ETransactionType.LINK_CARD;
    }

    public static boolean isTranferMoneyChannel() {
        return transactionType == ETransactionType.WALLET_TRANSFER;
    }

    public static boolean isTopupChannel() {
        return transactionType == ETransactionType.TOPUP;
    }

    public static boolean isWithDrawChannel() {
        return transactionType == ETransactionType.WITHDRAW;
    }

    public static boolean isPayChannel() {
        return transactionType == ETransactionType.PAY;
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

    public static boolean updateResultNetworkingError(String pMessage) {
        boolean isOffNetworking;

        try {
            isOffNetworking = !ConnectionUtil.isOnline(BasePaymentActivity.getCurrentActivity());
        } catch (Exception ex) {
            Log.e("updateResultNetworkingError", ex);

            isOffNetworking = false;
        }

        if (isOffNetworking && (pMessage.equals(GlobalData.getStringResource(RS.string.zingpaysdk_alert_no_connection))) ||
                pMessage.equals(GlobalData.getStringResource(RS.string.zpw_alert_networking_off_in_transaction))) {
            setResultNoInternet();
        }

        return isOffNetworking;
    }

    //region set transaction result to notify to app
    public static void setResult(EPaymentStatus pStatus) {
        GlobalData.getPaymentResult().paymentStatus = pStatus;
    }

    public static void setResultSuccess() {
        GlobalData.getPaymentResult().paymentStatus = EPaymentStatus.ZPC_TRANXSTATUS_SUCCESS;
    }

    public static void setResultFail() {
        GlobalData.getPaymentResult().paymentStatus = EPaymentStatus.ZPC_TRANXSTATUS_FAIL;
    }

    public static void setResultMoneyNotEnough() {
        GlobalData.getPaymentResult().paymentStatus = EPaymentStatus.ZPC_TRANXSTATUS_MONEY_NOT_ENOUGH;
    }

    public static void setResultProcessing() {
        GlobalData.getPaymentResult().paymentStatus = EPaymentStatus.ZPC_TRANXSTATUS_PROCESSING;
    }

    public static void setResultUpgrade() {
        GlobalData.getPaymentResult().paymentStatus = EPaymentStatus.ZPC_TRANXSTATUS_UPGRADE;
    }

    public static void setResultInvalidInput() {
        GlobalData.getPaymentResult().paymentStatus = EPaymentStatus.ZPC_TRANXSTATUS_INPUT_INVALID;
    }

    public static void setResultInvalidToken() {
        GlobalData.getPaymentResult().paymentStatus = EPaymentStatus.ZPC_TRANXSTATUS_TOKEN_INVALID;
    }

    public static void setResultLockUser() {
        GlobalData.getPaymentResult().paymentStatus = EPaymentStatus.ZPC_TRANXSTATUS_LOCK_USER;
    }

    public static void setResultServiceMaintenance() {
        GlobalData.getPaymentResult().paymentStatus = EPaymentStatus.ZPC_TRANXSTATUS_SERVICE_MAINTENANCE;
    }

    public static void setResultUserClose() {
        GlobalData.getPaymentResult().paymentStatus = EPaymentStatus.ZPC_TRANXSTATUS_CLOSE;
    }

    public static void setResultUpgradeAndSave() {
        GlobalData.getPaymentResult().paymentStatus = EPaymentStatus.ZPC_TRANXSTATUS_UPGRADE_SAVECARD;
    }

    public static void setResultNeedToLinkCard() {
        GlobalData.getPaymentResult().paymentStatus = EPaymentStatus.ZPC_TRANXSTATUS_NEED_LINKCARD;
    }

    public static void setResultNoInternet() {
        GlobalData.getPaymentResult().paymentStatus = EPaymentStatus.ZPC_TRANXSTATUS_NO_INTERNET;
    }

    public static void setResultNeedToLinkAccount() {
        GlobalData.getPaymentResult().paymentStatus = EPaymentStatus.ZPC_TRANXSTATUS_NEED_LINK_ACCOUNT;
    }

    public static void setResultNeedToLinkAccountBeforePayment() {
        GlobalData.getPaymentResult().paymentStatus = EPaymentStatus.ZPC_TRANXSTATUS_NEED_LINK_ACCOUNT_BEFORE_PAYMENT;
    }

    public static void setResultUpLevelLinkAccountAndPayment() {
        GlobalData.getPaymentResult().paymentStatus = EPaymentStatus.ZPC_TRANXSTATUS_UPLEVEL_AND_LINK_BANKACCOUNT_CONTINUE_PAYMENT;
    }

    public static void setResultNeedToLinkCardBeforePayment() {
        GlobalData.getPaymentResult().paymentStatus = EPaymentStatus.ZPC_TRANXSTATUS_NEED_LINKCARD_BEFORE_PAYMENT;
    }
    //endregion

    /***
     * is this app configured by backend.
     */
    public static boolean isAllowApplication() {
        try {
            DAppInfo currentApp = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getAppById(String.valueOf(appID)), DAppInfo.class);

            return (currentApp != null && currentApp.isAllow()) ? true : false;

        } catch (Exception e) {
            Log.e("isAllowApplication", e);
        }
        return false;
    }

    /***
     * this is red package channel
     */
    public static boolean isRedPacketChannel() {
        long redPackageID = 0;

        try {
            redPackageID = Long.parseLong(GlobalData.getStringResource(RS.string.zpw_redpackage_app_id));

            return redPackageID == GlobalData.appID;
        } catch (Exception ex) {
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
        } catch (Exception ex) {

        }
        return false;
    }

    /***
     * Get transtype of payment.
     */
    public static ETransactionType getTransactionType() {
        //link account bank.
        if (mPaymentOption != null &&
                !TextUtils.isEmpty(mPaymentOption.getIncludePaymentMethodType()) &&
                mPaymentOption.getIncludePaymentMethodType().equals(getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_link_acc))) {
            transactionType = ETransactionType.LINK_ACC;
            bankFunction = EBankFunction.LINK_BANK_ACCOUNT;
            return ETransactionType.LINK_ACC;
        }

        //link card.
        if (mPaymentOption != null &&
                !TextUtils.isEmpty(mPaymentOption.getIncludePaymentMethodType()) &&
                mPaymentOption.getIncludePaymentMethodType().equals(getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_link_card))) {
            appID = Long.parseLong(GlobalData.getStringResource(RS.string.zpw_conf_wallet_id));
            transactionType = ETransactionType.LINK_CARD;
            bankFunction = EBankFunction.LINK_CARD;
            return ETransactionType.LINK_CARD;
        }

        //wallet tranfer.
        if (mPaymentOption != null &&
                !TextUtils.isEmpty(mPaymentOption.getIncludePaymentMethodType()) &&
                mPaymentOption.getIncludePaymentMethodType().equals(getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_wallet_transfer))) {
            transactionType = ETransactionType.WALLET_TRANSFER;
            bankFunction = EBankFunction.PAY;
            return ETransactionType.WALLET_TRANSFER;
        }

        //withdraw.
        if (mPaymentOption != null &&
                !TextUtils.isEmpty(mPaymentOption.getIncludePaymentMethodType()) &&
                mPaymentOption.getIncludePaymentMethodType().equals(getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_withdraw))) {
            transactionType = ETransactionType.WITHDRAW;
            bankFunction = EBankFunction.WITHDRAW;
            return ETransactionType.WITHDRAW;
        }

        //topup.
        if (GlobalData.appID == Long.parseLong(getStringResource(RS.string.zpw_conf_wallet_id))) {
            transactionType = ETransactionType.TOPUP;
            bankFunction = EBankFunction.PAY;
            return ETransactionType.TOPUP;
        }

        //pay. with other case.
        transactionType = ETransactionType.PAY;
        bankFunction = EBankFunction.PAY;
        return ETransactionType.PAY;
    }

    public static EBankFunction getPayBankFunction(DPaymentChannelView pChannel) {
        if (pChannel.isBankAccountMap()) {
            bankFunction = EBankFunction.PAY_BY_BANKACCOUNT_TOKEN;
        } else if (pChannel.isMapCardChannel()) {
            bankFunction = EBankFunction.PAY_BY_CARD_TOKEN;
        } else if (pChannel.isBankAccount()) {
            bankFunction = EBankFunction.PAY_BY_BANK_ACCOUNT;
        } else {
            bankFunction = EBankFunction.PAY_BY_CARD;
        }

        return bankFunction;
    }

    public static EBankFunction getBankFunctionPay() {
        if (GlobalData.isMapBankAccountChannel()) {
            bankFunction = EBankFunction.PAY_BY_BANKACCOUNT_TOKEN;
        } else if (GlobalData.isMapCardChannel()) {
            bankFunction = EBankFunction.PAY_BY_CARD_TOKEN;
        } else {
            bankFunction = EBankFunction.PAY_BY_CARD;
        }
        return bankFunction;
    }

    public static void initApplication(ZPWPaymentInfo pPaymentInfo) {
        Log.d("GlobalData", "initApplication(ZPWPaymentInfo pPaymentInfo)()");
        GlobalData.mPaymentInfo = pPaymentInfo;
        GlobalData.mPaymentOption = new ZPPaymentOption(null);

        initResultReturn();
    }

    public static void initApplicationUserInfo(ZPWPaymentInfo pPaymentInfo) {
        if (GlobalData.mPaymentInfo != null && GlobalData.mPaymentInfo.userInfo != null) {
            GlobalData.mPaymentInfo.userInfo.zaloPayUserId = pPaymentInfo.userInfo.zaloPayUserId;
            GlobalData.mPaymentInfo.userInfo.accessToken = pPaymentInfo.userInfo.accessToken;
        } else {
            Log.d("GlobalData", "initApplicationUserInfo(ZPWPaymentInfo pPaymentInfo)");
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

    public static void initApplication(ZPWPaymentInfo pPaymentInfo, ZPWSaveMapCardListener pListener) {
        Log.d("GlobalData", "initApplication(ZPWPaymentInfo pPaymentInfo, ZPWSaveMapCardListener pListener)");
        GlobalData.mPaymentInfo = pPaymentInfo;
        GlobalData.saveMapCardListener = pListener;
    }
    //endregion

    public static void initResultReturn() {
        GlobalData.setResultFail();
    }

    /***
     * alwaw call this to set static listener and info.
     */
    public static void setSDKData(Activity pActivity, ZPPaymentListener pPaymentListener, ZPWPaymentInfo pPaymentInfo, ZPPaymentOption pPaymentOption) throws Exception {
        if (!isAccessRight()) {
            throw new Exception("Violate Design Pattern! Only 'pay' static method of ZingPayService class can set application!");
        }

        GlobalData.appID = pPaymentInfo.appID;
        GlobalData.mMerchantActivity = new WeakReference<Activity>(pActivity);
        GlobalData.mListener = pPaymentListener;
        GlobalData.mPaymentInfo = pPaymentInfo;
        GlobalData.mPaymentOption = pPaymentOption;

        //reset data
        GlobalData.paymentResult = null;
        GlobalData.mTransactionPin = null;
        GlobalData.mUserProfile = null;
        GlobalData.orderAmountTotal = pPaymentInfo.amount;
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
            return WalletSDKApplication.getZaloPayContext();
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
        if (GlobalData.mPaymentInfo == null || GlobalData.mPaymentInfo.userInfo == null) {
            if (GlobalData.getPaymentResult() != null) {
                GlobalData.setResultInvalidInput();
            }

            if (BasePaymentActivity.getCurrentActivity() instanceof BasePaymentActivity) {
                ((BasePaymentActivity) BasePaymentActivity.getCurrentActivity()).recycleActivity();
            }

            Log.e("getPaymentInfo", "===mPaymentInfo.userInfo=NULL");

            return null;

        }

        return GlobalData.mPaymentInfo;
    }

    public static boolean isForceChannel() {
        if (getPaymentInfo() != null) {
            return getPaymentInfo().isForceChannel();
        }
        return false;
    }

    public static long getOrderAmount() {
        if (getPaymentInfo() != null)
            return getPaymentInfo().amount;

        return 0;
    }

    public static ZPPaymentResult getPaymentResult() {
        if (GlobalData.paymentResult == null) {
            GlobalData.paymentResult = new ZPPaymentResult(mPaymentInfo, EPaymentStatus.ZPC_TRANXSTATUS_FAIL);
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

        return userProfile.allow == true ? Constants.LEVELMAP_ALLOW : Constants.LEVELMAP_BAN;
    }

    /***
     * check whether creditcard channel require otp?
     */
    public static int isRequireOtpCreditCard() {
        BankConfig bankConfig = null;
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
                        && getUserProfileList().profilelevelpermisssion.get(i).transtype == Integer.parseInt(transactionType.toString())) {
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

    public static ZPWNotification getNotification() {
        if (mNotification == null) {
            mNotification = new ZPWNotification(0, "");
        }
        return mNotification;
    }

    public static void setNotification(ZPWNotification mNotify) {
        GlobalData.mNotification = mNotify;
    }

    public static boolean isAllowShowWebviewVCB() {
        try {
            BankConfig bankConfig = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getBankConfig(GlobalData.getPaymentInfo().linkAccInfo.getBankCode()), BankConfig.class);
            if(bankConfig != null && !bankConfig.isCoverBank()){
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    public static EBankFunction getCurrentBankFunction() {
        return bankFunction;
    }

    public static void setCurrentBankFunction(EBankFunction pBankFunction) {
        bankFunction = pBankFunction;
    }
    //endregion
}

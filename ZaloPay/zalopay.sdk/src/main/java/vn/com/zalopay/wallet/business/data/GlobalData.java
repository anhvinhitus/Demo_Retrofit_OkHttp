package vn.com.zalopay.wallet.business.data;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import java.lang.ref.WeakReference;

import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.business.feedback.FeedBackCollector;
import vn.com.zalopay.wallet.business.feedback.IFeedBack;
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
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.tracker.ZPAnalyticsTrackerWrapper;
import vn.com.zalopay.wallet.view.component.activity.BasePaymentActivity;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

/***
 * static class contain static data.
 * need to dispose everything after quit sdk
 */
public class GlobalData {
    @CardChannel
    public static int cardChannelType = CardChannel.ATM;
    public static ZPAnalyticsTrackerWrapper analyticsTrackerWrapper;
    @TransactionType
    public static int mTranstype;
    //callback to merchant after sdk retry load gateway info
    protected static WeakReference<ZPWGatewayInfoCallback> mMerchantCallBack;
    @BankFunctionCode
    private static int bankFunction = BankFunctionCode.PAY;
    private static WeakReference<Activity> mMerchantActivity = null;
    private static ZPPaymentListener mListener = null;
    private static String mTransactionPin = null;
    private static WeakReference<IChannelActivityCallBack> mChannelActivityCallBack;

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

    public static boolean isNewUser(String zalopay_userid) {
        String userID;
        try {
            userID = SharedPreferencesManager.getInstance().getCurrentUserID();
        } catch (Exception e) {
            Log.e("isNewUser", e);
            return true;
        }
        return TextUtils.isEmpty(userID) || !userID.equals(zalopay_userid);
    }

    //region is channel

    public static boolean isChannelHasInputCard(PaymentInfoHelper paymentInfoHelper) {
        boolean isTransactionHasInputCard = !paymentInfoHelper.payByCardMap() && !paymentInfoHelper.payByBankAccountMap() && !paymentInfoHelper.isWithDrawTrans();
        boolean isChannelHasInputCard = true;
        if (BasePaymentActivity.getCurrentActivity() instanceof PaymentChannelActivity
                && ((PaymentChannelActivity) BasePaymentActivity.getCurrentActivity()).getAdapter().isZaloPayFlow()) {
            isChannelHasInputCard = false;
        }
        return isTransactionHasInputCard && isChannelHasInputCard;
    }
    //endregion

    public static String getOfflineMessage(PaymentInfoHelper paymentInfoHelper) {
        if (paymentInfoHelper.bankAccountLink()) {
            return GlobalData.getStringResource(RS.string.sdk_alert_networking_off_in_link_account);
        } else if (paymentInfoHelper.bankAccountUnlink()) {
            return GlobalData.getStringResource(RS.string.sdk_alert_networking_off_in_unlink_account);
        } else {
            return GlobalData.getStringResource(RS.string.zpw_alert_networking_off_in_transaction);
        }
    }

    public static boolean updateResultNetworkingError(PaymentInfoHelper paymentInfoHelper, String pMessage) {
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
            paymentInfoHelper.setResult(PaymentStatus.DISCONNECT);
        }
        return isOffNetworking;
    }

    /***
     * is this app configured by backend.
     */
    public static boolean isAllowApplication(long appID) {
        try {
            AppInfo currentApp = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getAppById(String.valueOf(appID)), AppInfo.class);

            return (currentApp != null && currentApp.isAllow());

        } catch (Exception e) {
            Log.e("isAllowApplication", e);
        }
        return false;
    }

    /***
     * this is red package channel
     */
    public static boolean isRedPacketChannel(long appID) {
        long redPackageID;
        try {
            redPackageID = Long.parseLong(GlobalData.getStringResource(RS.string.zpw_redpackage_app_id));
            return redPackageID == appID;
        } catch (Exception ignored) {
        }
        return false;
    }

    public static boolean isZalopayChannel(long appID) {
        return BuildConfig.channel_zalopay == appID;
    }

    public static void selectBankFunctionByTransactionType(PaymentInfoHelper paymentInfoHelper) {
        if (paymentInfoHelper.isBankAccountTrans()) {
            bankFunction = BankFunctionCode.LINK_BANK_ACCOUNT;
            return;
        }
        switch (paymentInfoHelper.getTranstype()) {
            case TransactionType.LINK:
                bankFunction = BankFunctionCode.LINK_CARD;
                break;
            case TransactionType.WITHDRAW:
                bankFunction = BankFunctionCode.WITHDRAW;
                break;
            case TransactionType.MONEY_TRANSFER:
            case TransactionType.TOPUP:
            case TransactionType.PAY:
                bankFunction = BankFunctionCode.PAY;
                break;
        }
    }

    public static void initializeAnalyticTracker(long pAppId, String pAppTransID, @TransactionType int transactionType) {
        analyticsTrackerWrapper = new ZPAnalyticsTrackerWrapper(pAppId, pAppTransID, transactionType);
    }

    @BankFunctionCode
    public static int getPayBankFunction(PaymentChannel pChannel) {
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
    public static int getBankFunctionPay(PaymentInfoHelper paymentInfoHelper) {
        if (paymentInfoHelper.payByBankAccountMap()) {
            bankFunction = BankFunctionCode.PAY_BY_BANKACCOUNT_TOKEN;
        } else if (paymentInfoHelper.payByCardMap()) {
            bankFunction = BankFunctionCode.PAY_BY_CARD_TOKEN;
        } else {
            bankFunction = BankFunctionCode.PAY_BY_CARD;
        }
        return bankFunction;
    }

    /***
     * alwaw call this to set static listener and info.
     */
    public static void setSDKData(Activity pActivity, ZPPaymentListener pPaymentListener, @TransactionType int pTranstype) throws Exception {
        if (!isAccessRight()) {
            throw new Exception("Violate Design Pattern! Only 'pay' static method of ZingPayService class can set application!");
        }
        GlobalData.mMerchantActivity = new WeakReference<>(pActivity);
        GlobalData.mListener = pPaymentListener;
        GlobalData.mTransactionPin = null;
        AdapterBase.existedMapCard = false;
        GlobalData.mTranstype = pTranstype;
    }

    public static void setIFingerPrint(IPaymentFingerPrint pFingerPrintFromMerchant) {
        PaymentFingerPrint.shared().setPaymentFingerPrint(pFingerPrintFromMerchant);
    }

    public static void setFeedBack(IFeedBack pFeedBack) {
        FeedBackCollector.shared().setFeedBack(pFeedBack);
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
        if (mMerchantActivity != null) {
            return mMerchantActivity.get();
        }
        return null;
    }

    public static String getTransactionPin() {
        return mTransactionPin;
    }

    public static void setTransactionPin(String mTransactionPin) {
        GlobalData.mTransactionPin = mTransactionPin;
    }

    public static ZPPaymentListener getPaymentListener() {
        return mListener;
    }

    public static String getTransProcessingMessage(@TransactionType int pTranstype) {
        return pTranstype == TransactionType.LINK ? RS.string.zingpaysdk_alert_processing_get_status_linkcard_fail : RS.string.zingpaysdk_alert_processing_get_status_fail;
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

    public static boolean shouldNativeWebFlow() {
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

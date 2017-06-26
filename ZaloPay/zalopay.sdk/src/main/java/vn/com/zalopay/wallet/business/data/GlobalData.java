package vn.com.zalopay.wallet.business.data;

import android.app.Activity;
import android.content.Context;

import java.lang.ref.WeakReference;

import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.business.feedback.FeedBackCollector;
import vn.com.zalopay.wallet.business.feedback.IFeedBack;
import vn.com.zalopay.wallet.business.fingerprint.IPaymentFingerPrint;
import vn.com.zalopay.wallet.business.fingerprint.PaymentFingerPrint;
import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.constants.CardChannel;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.controller.SDKPayment;
import vn.com.zalopay.wallet.listener.ZPPaymentListener;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.tracker.ZPAnalyticsTrackerWrapper;
import vn.com.zalopay.wallet.ui.BaseActivity;
import vn.com.zalopay.wallet.ui.channel.ChannelActivity;

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
    @BankFunctionCode
    private static int bankFunction = BankFunctionCode.PAY;
    private static WeakReference<Activity> mMerchantActivity = null;
    private static ZPPaymentListener mListener = null;

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
        return BaseActivity.getActivityCount() > 0 || BaseActivity.getActivityCount() > 0;
    }

    public static boolean isChannelHasInputCard(PaymentInfoHelper paymentInfoHelper) {
        boolean isTransactionHasInputCard = !paymentInfoHelper.payByCardMap() && !paymentInfoHelper.payByBankAccountMap() && !paymentInfoHelper.isWithDrawTrans();
        boolean isChannelHasInputCard = true;
        if (BaseActivity.getCurrentActivity() instanceof ChannelActivity
                && ((ChannelActivity) BaseActivity.getCurrentActivity()).getAdapter().isZaloPayFlow()) {
            isChannelHasInputCard = false;
        }
        return isTransactionHasInputCard && isChannelHasInputCard;
    }

    public static String getOfflineMessage(PaymentInfoHelper paymentInfoHelper) {
        if (paymentInfoHelper.bankAccountLink()) {
            return GlobalData.getStringResource(RS.string.sdk_alert_networking_off_in_link_account);
        } else if (paymentInfoHelper.bankAccountUnlink()) {
            return GlobalData.getStringResource(RS.string.sdk_alert_networking_off_in_unlink_account);
        } else {
            return GlobalData.getStringResource(RS.string.zpw_alert_networking_off_in_transaction);
        }
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
        GlobalData.mTranstype = pTranstype;
    }

    public static void setIFingerPrint(IPaymentFingerPrint pFingerPrintFromMerchant) {
        PaymentFingerPrint.shared().setPaymentFingerPrint(pFingerPrintFromMerchant);
    }

    public static void setFeedBack(IFeedBack pFeedBack) {
        FeedBackCollector.shared().setFeedBack(pFeedBack);
    }

    public static Context getAppContext() {
        return SDKApplication.getApplication();
    }

    public static Activity getMerchantActivity() {
        if (mMerchantActivity != null) {
            return mMerchantActivity.get();
        }
        return null;
    }

    public static ZPPaymentListener getPaymentListener() {
        return mListener;
    }

    public static String getTransProcessingMessage(@TransactionType int pTranstype) {
        return pTranstype == TransactionType.LINK ?
                RS.string.sdk_fail_trans_status_link : RS.string.sdk_fail_trans_status;
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

package vn.com.zalopay.wallet.business.data;

import android.app.Activity;
import android.content.Context;

import java.lang.ref.WeakReference;

import timber.log.Timber;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.analytics.ZPScreens;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.ELinkAccType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.business.entity.linkacc.LinkAccInfo;
import vn.com.zalopay.wallet.business.fingerprint.IPaymentFingerPrint;
import vn.com.zalopay.wallet.business.fingerprint.PaymentFingerPrint;
import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.constants.CardChannel;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.controller.SDKPayment;
import vn.com.zalopay.wallet.feedback.FeedBackCollector;
import vn.com.zalopay.wallet.feedback.IFeedBack;
import vn.com.zalopay.wallet.helper.TrackHelper;
import vn.com.zalopay.wallet.helper.TransactionHelper;
import vn.com.zalopay.wallet.listener.ZPPaymentListener;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.repository.ResourceManager;
import vn.com.zalopay.wallet.tracker.ZPAnalyticsTrackerWrapper;
import vn.com.zalopay.wallet.ui.BaseActivity;

/***
 * static class contain static data.
 * need to dispose everything after quit sdk
 */
public class GlobalData {
    @CardChannel
    public static int cardChannelType = CardChannel.ATM;
    public static ZPAnalyticsTrackerWrapper analyticsTrackerWrapper;
    public static PaymentInfoHelper paymentInfoHelper;
    public static boolean mShowFingerPrintToast = false;
    @BankFunctionCode
    private static int bankFunction = BankFunctionCode.PAY;
    private static WeakReference<Activity> mMerchantActivity = null;
    private static ZPPaymentListener mListener = null;

    public static
    @TransactionType
    int transtype() {
        return paymentInfoHelper != null ? paymentInfoHelper.getTranstype() : TransactionType.PAY;
    }

    public static void updatePaymentInfo(boolean isBankAccount) {
        if (paymentInfoHelper == null) {
            return;
        }
        paymentInfoHelper.setTranstype(TransactionType.LINK);
        if (isBankAccount) {
            LinkAccInfo linkAccInfo = new LinkAccInfo(CardType.PVCB, ELinkAccType.LINK);
            paymentInfoHelper.setLinkAccountInfo(linkAccInfo);
        }
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
        return BaseActivity.getActivityCount() > 0;
    }

    public static void updateBankFuncByTranstype() {
        if (paymentInfoHelper == null) {
            return;
        }
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

    @BankFunctionCode
    public static int updateBankFuncByChannel(PaymentChannel pChannel) {
        if (pChannel == null) {
            return bankFunction;
        }
        if (pChannel.isBankAccountMap()) {
            bankFunction = BankFunctionCode.PAY_BY_BANKACCOUNT_TOKEN;
        } else if (pChannel.isMapCardChannel()) {
            bankFunction = BankFunctionCode.PAY_BY_CARD_TOKEN;
        }
        return bankFunction;
    }

    public static void initializeAnalyticTracker(String pUserId, long pAppId, String pAppTransID,
                                                 @TransactionType int transactionType, int orderSource) {
        analyticsTrackerWrapper = new ZPAnalyticsTrackerWrapper(pUserId, pAppId, pAppTransID, transactionType, orderSource);
    }

    /***
     * alwaw call this to set static listener and info.
     */
    public static void setSDKData(Activity pActivity, ZPPaymentListener pPaymentListener) throws Exception {
        if (!isAccessRight()) {
            throw new Exception("Violate Design Pattern! Only 'pay' static method of ZingPayService class can set application!");
        }
        GlobalData.mMerchantActivity = new WeakReference<>(pActivity);
        GlobalData.mListener = pPaymentListener;
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

    public static Activity getMerchantActivity() throws Exception {
        if (mMerchantActivity == null || mMerchantActivity.get() == null) {
            throw new IllegalStateException("Merchant activity invalid");
        }
        return mMerchantActivity.get();
    }

    public static ZPPaymentListener getPaymentListener() {
        return mListener;
    }

    public static String getStringResource(String pResourceID) {
        try {
            // Try to get string from resource sent from before get from local
            String result = ResourceManager.getInstance(null).getString(pResourceID);
            return (result != null) ? result : getAppContext().getString(RS.getString(pResourceID));
        } catch (Exception e) {
            Timber.w(e, "Exception get resource string");
        }
        return null;
    }

    @BankFunctionCode
    public static int getCurrentBankFunction() {
        return bankFunction;
    }

    public static void extraJobOnPaymentCompleted(StatusResponse pStatusResponse, String pBankCode) {
        if (pStatusResponse == null || paymentInfoHelper == null) {
            return;
        }
        try {
            boolean success = TransactionHelper.isTransactionSuccess(pStatusResponse);
            //notify to app to do some background task
            if (GlobalData.getPaymentListener() != null) {
                GlobalData.getPaymentListener().onPreComplete(success, pStatusResponse.zptransid, paymentInfoHelper.getAppTransId());
            }
            //track app trans id
            TrackHelper.trackingTransactionEvent(success ? ZPPaymentSteps.OrderStepResult_Success : ZPPaymentSteps.OrderStepResult_Fail
                    , pStatusResponse
                    , pBankCode);
            //track screen name
            String screenName = paymentInfoHelper.isLinkTrans() ? ZPScreens.BANK_RESULT : ZPScreens.PAYMENT_RESULT;
            ZPAnalytics.trackScreen(screenName);
            TrackHelper.trackPaymentResult(paymentInfoHelper);
        } catch (Exception e) {
            Timber.w(e);
        }
    }
    //endregion
}

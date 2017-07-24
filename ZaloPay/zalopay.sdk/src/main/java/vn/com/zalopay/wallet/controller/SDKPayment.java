package vn.com.zalopay.wallet.controller;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.zalopay.ui.widget.dialog.DialogManager;

import timber.log.Timber;
import vn.com.vng.zalopay.monitors.ZPMonitorEvent;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.error.CError;
import vn.com.zalopay.wallet.business.fingerprint.IPaymentFingerPrint;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.PaymentError;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.feedback.IFeedBack;
import vn.com.zalopay.wallet.interactor.ChannelListInteractor;
import vn.com.zalopay.wallet.listener.ZPPaymentListener;
import vn.com.zalopay.wallet.objectmanager.SingletonLifeCircleManager;
import vn.com.zalopay.wallet.paymentinfo.IPaymentInfo;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.ui.BaseActivity;
import vn.com.zalopay.wallet.ui.channel.ChannelActivity;
import vn.com.zalopay.wallet.ui.channellist.ChannelListActivity;
import vn.com.zalopay.wallet.ui.channellist.ResultPaymentFragment;
import vn.com.zalopay.wallet.validation.IValidate;
import vn.com.zalopay.wallet.validation.PaymentInfoValidation;
import vn.com.zalopay.wallet.workflow.AbstractWorkFlow;

/***
 * payment controller class
 */
public class SDKPayment {
    /***
     * merchant check whether
     * use is opening sdk for payment
     */
    public synchronized static boolean isOpenSdk() {
        return GlobalData.isUserInSDK();
    }

    /***
     * merchant need to call this to check whether
     * can close sdk right away before calling closeSdk()
     */
    public synchronized static boolean canCloseSdk() {
        try {
            Activity sdkCurrentActivity = BaseActivity.getCurrentActivity();
            if (sdkCurrentActivity == null || sdkCurrentActivity.isFinishing()) {
                return true;
            }
            if (sdkCurrentActivity instanceof ChannelListActivity) {
                Fragment fragment = ((ChannelListActivity) sdkCurrentActivity).getActiveFragment();
                return fragment instanceof ResultPaymentFragment;
            }
            if (sdkCurrentActivity instanceof ChannelActivity) {
                AbstractWorkFlow adapterBase = ((ChannelActivity) sdkCurrentActivity).getWorkFlow();
                return adapterBase != null && adapterBase.isFinalScreen();
            }
            return true;
        } catch (Exception ex) {
            Timber.w(ex, "Exception check close SDK");
        }
        return false;
    }

    /***
     * merchant call this to close sdk right away
     */
    public synchronized static void closeSdk() throws Exception {
        if (!isOpenSdk()) {
            Timber.d("SDK không đang mở, yêu cầu đóng sdk không thể thực hiện");
            return;
        }
        if (!canCloseSdk()) {
            Timber.d("Có 1 hóa đơn vẫn đang xử lý. Không thể đóng giao dịch khi chưa hoàn thành");
            return;
        }
        try {
            Activity sdkCurrentActivity = BaseActivity.getCurrentActivity();
            if (sdkCurrentActivity == null || sdkCurrentActivity.isFinishing()) {
                return;
            }
            if (sdkCurrentActivity instanceof ChannelListActivity) {
                Fragment fragment = ((ChannelListActivity) sdkCurrentActivity).getActiveFragment();
                if (fragment instanceof ResultPaymentFragment) {
                    ((ResultPaymentFragment) fragment).callbackThenTerminate();
                }
            } else if (sdkCurrentActivity instanceof ChannelActivity) {
                AbstractWorkFlow adapterBase = ((ChannelActivity) sdkCurrentActivity).getWorkFlow();
                if (adapterBase != null) {
                    adapterBase.onClickSubmission();
                }
            }
        } catch (Exception ex) {
            Timber.w(ex, "Exception close SDK");
            throw ex;
        }
    }

    /**
     * entry point for payment sdk
     *
     * @param pMerchantActivity
     * @param pPaymentInfo
     * @param pPaymentListener
     * @param pExtraParams
     */
    public synchronized static void pay(final Activity pMerchantActivity, IPaymentInfo pPaymentInfo, final ZPPaymentListener pPaymentListener, Object... pExtraParams) {
        SDKApplication.getApplicationComponent().monitorEventTiming().recordEvent(ZPMonitorEvent.TIMING_SDK_START);
        //validate payment info and activity
        if (pMerchantActivity == null || pPaymentInfo == null) {
            if (pPaymentListener != null) {
                Timber.w("Component (activity,payment info) is null");
                pPaymentListener.onError(new CError(PaymentError.COMPONENT_NULL, "Dữ liệu không hợp lệ"));
            }
            return;
        }

        //check internet connection
        if (!ConnectionUtil.isOnline(pMerchantActivity)) {
            if (GlobalData.getPaymentListener() != null) {
                GlobalData.getPaymentListener().onError(new CError(PaymentError.NETWORKING_ERROR,
                        GlobalData.getAppContext().getResources().getString(R.string.sdk_payment_no_internet_mess)));
            }
            SingletonLifeCircleManager.disposeAll();
            return;
        }

        PaymentInfoHelper paymentInfoHelper = new PaymentInfoHelper(pPaymentInfo);
        IValidate validation = new PaymentInfoValidation(paymentInfoHelper);
        //validate params order info and user info
        String validateMessage = validation.onValidate(pPaymentInfo);
        if (!TextUtils.isEmpty(validateMessage)) {
            terminateSession(validateMessage, PaymentError.DATA_INVALID);
            return;
        }
        //set listener and data payment to global static
        try {
            GlobalData.setSDKData(pMerchantActivity, pPaymentListener);
        } catch (Exception e) {
            terminateSession(pMerchantActivity.getResources().getString(R.string.sdk_invalid_payment_data), PaymentError.DATA_INVALID);
            return;
        }
        //set fingerprint listener from merchant
        if (pExtraParams != null && pExtraParams.length > 0) {
            for (Object pExtraParam : pExtraParams) {
                if (pExtraParam instanceof IPaymentFingerPrint) {
                    GlobalData.setIFingerPrint((IPaymentFingerPrint) pExtraParam);
                }
                if (pExtraParam instanceof IFeedBack) {
                    GlobalData.setFeedBack((IFeedBack) pExtraParam);
                }
            }
        }
        Log.d("pay", "payment info", paymentInfoHelper);
        startGateway(paymentInfoHelper);
    }

    private static void startGateway(PaymentInfoHelper paymentInfoHelper) {
        Activity merchantActivity = GlobalData.getMerchantActivity();
        if (merchantActivity == null || merchantActivity.isFinishing()) {
            Timber.w("merchant activity is null");
            terminateSession(GlobalData.getAppContext().getResources().getString(R.string.sdk_invalid_payment_data), PaymentError.DATA_INVALID);
            return;
        }
        Intent intent;
        //this is link , go to channel directly
        if (paymentInfoHelper.isLinkTrans()) {
            intent = new Intent(GlobalData.getAppContext(), ChannelActivity.class);
            int layoutId = paymentInfoHelper.isBankAccountTrans() ? R.layout.screen__link__acc : R.layout.screen__card;
            intent.putExtra(Constants.CHANNEL_CONST.layout, layoutId);
        } else {
            intent = new Intent(merchantActivity, ChannelListActivity.class);
        }
        //init tracker event
        long appId = paymentInfoHelper.getAppId();
        String appTransId = paymentInfoHelper.getAppTransId();
        int orderSource = paymentInfoHelper.getOrderSource();
        int transtype = paymentInfoHelper.getTranstype();
        GlobalData.initializeAnalyticTracker(appId, appTransId, transtype, orderSource);

        // here start background task to collect data
        ChannelListInteractor interactor = SDKApplication.getApplicationComponent().channelListInteractor();
        interactor.collectPaymentInfo(paymentInfoHelper);

        SDKApplication.getApplicationComponent().monitorEventTiming().recordEvent(ZPMonitorEvent.TIMING_SDK_START_ACTIVITY);
        paymentInfoHelper.setResult(PaymentStatus.PROCESSING);
        GlobalData.paymentInfoHelper = paymentInfoHelper;
        GlobalData.updateBankFuncByTranstype();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        merchantActivity.startActivity(intent);
        Timber.d("start screen %s", intent.getComponent().getShortClassName());
    }

    /***
     * show dialog and dispose sdk in error cases
     * @param pMessage
     */
    private static void terminateSession(final String pMessage, @PaymentError int pPayError) {
        DialogManager.closeLoadDialog();
        if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onError(new CError(pPayError, pMessage));
        }
        SingletonLifeCircleManager.disposeAll();
        SDKApplication.getApplicationComponent().channelListInteractor().cleanup();
    }
}

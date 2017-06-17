package vn.com.zalopay.wallet.controller;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import com.zalopay.ui.widget.dialog.DialogManager;
import com.zalopay.ui.widget.dialog.SweetAlertDialog;

import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.error.CError;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.business.feedback.IFeedBack;
import vn.com.zalopay.wallet.business.fingerprint.IPaymentFingerPrint;
import vn.com.zalopay.wallet.business.objectmanager.SingletonLifeCircleManager;
import vn.com.zalopay.wallet.business.validation.IValidate;
import vn.com.zalopay.wallet.business.validation.PaymentInfoValidation;
import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.constants.PaymentError;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.listener.ZPPaymentListener;
import vn.com.zalopay.wallet.paymentinfo.IPaymentInfo;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.ui.channellist.ChannelListActivity;
import vn.com.zalopay.wallet.view.component.activity.BasePaymentActivity;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

import static vn.com.zalopay.wallet.constants.Constants.PMC_CONFIG;

/***
 * payment controller class
 */
public class SDKPayment {
    /***
     * merchant check whether
     * use is opening sdk for payment
     * @return
     */
    public synchronized static boolean isOpenSdk() {
        return GlobalData.isUserInSDK();
    }

    /***
     * merchant need to call this to check whether
     * can close sdk right away before calling closeSdk()
     * @return
     */
    public synchronized static boolean canCloseSdk() {
        try {
            if (BasePaymentActivity.getCurrentActivity() instanceof PaymentChannelActivity
                    && ((PaymentChannelActivity) BasePaymentActivity.getCurrentActivity()).getAdapter() != null) {
                return ((PaymentChannelActivity) BasePaymentActivity.getCurrentActivity()).getAdapter().isFinalScreen();
            }
        } catch (Exception ex) {
            Log.e("canCloseSdk", ex);
        }
        return false;
    }

    /***
     * merchant call this to close sdk right away
     *
     * @throws Exception
     */
    public synchronized static void closeSdk() throws Exception {
        if (!isOpenSdk()) {
            throw new Exception("SDK không đang mở, yêu cầu đóng sdk không thể thực hiện");
        }
        if (!canCloseSdk()) {
            throw new Exception("Có 1 hóa đơn vẫn đang xử lý. Không thể đóng giao dịch khi chưa hoàn thành.");
        }
        try {
            if (BasePaymentActivity.getCurrentActivity() instanceof PaymentChannelActivity
                    && ((PaymentChannelActivity) BasePaymentActivity.getCurrentActivity()).getAdapter() != null) {
                ((PaymentChannelActivity) BasePaymentActivity.getCurrentActivity()).getAdapter().onClickSubmission();
            } else {
                throw new Exception("Không thể đóng sdk lúc này");
            }
        } catch (Exception ex) {
            Log.e("closeSdk", ex);
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

        //validate payment info and activity
        if (pMerchantActivity == null || pPaymentInfo == null) {
            if (pPaymentListener != null) {
                pPaymentListener.onError(new CError(PaymentError.COMPONENT_NULL, "Component (activity,payment info) is null"));
            }
            return;
        }

        //check internet connection
        if (!ConnectionUtil.isOnline(pMerchantActivity)) {
            if (GlobalData.getPaymentListener() != null) {
                GlobalData.getPaymentListener().onError(new CError(PaymentError.NETWORKING_ERROR, GlobalData.getStringResource(RS.string.zingpaysdk_alert_no_connection)));
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

        if (!bypassBankAccount(paymentInfoHelper)) {
            return;
        }

        //set listener and data payment to global static
        try {
            GlobalData.setSDKData(pMerchantActivity, pPaymentListener, paymentInfoHelper.getTranstype());
        } catch (Exception e) {
            terminateSession(pMerchantActivity.getResources().getString(R.string.zingpaysdk_alert_input_error), PaymentError.DATA_INVALID);
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
        Log.d("pay", "payment info ", paymentInfoHelper);
        GlobalData.selectBankFunctionByTransactionType(paymentInfoHelper);
        //init tracker event
        long appId = paymentInfoHelper.getAppId();
        int transtype = paymentInfoHelper.getTranstype();
        String appTransId = paymentInfoHelper.getAppTransId();
        GlobalData.initializeAnalyticTracker(appId, appTransId, transtype);

        startGateway(paymentInfoHelper);
    }

    private static boolean bypassBankAccount(PaymentInfoHelper paymentInfoHelper) {
        BankConfig bankConfig = SDKApplication
                .getApplicationComponent()
                .bankListInteractor()
                .getBankConfig(paymentInfoHelper.getLinkAccBankCode());
        //1 zalopay user has only 1 vcb account
        if (paymentInfoHelper.isBankAccountTrans()
                && paymentInfoHelper.bankAccountLink()
                && BankAccountHelper.hasBankAccountOnCache(paymentInfoHelper.getUserId(), paymentInfoHelper.getLinkAccBankCode())) {
            DialogManager.closeProcessDialog();
            DialogManager.showSweetDialog(GlobalData.getMerchantActivity(), SweetAlertDialog.INFO_TYPE, GlobalData.getMerchantActivity().getString(R.string.dialog_title_cannot_connect),
                    GlobalData.getMerchantActivity().getString(R.string.zpw_warning_link_bankaccount_existed), pIndex -> {
                        paymentInfoHelper.setResult(PaymentStatus.USER_CLOSE);
                        if (GlobalData.getPaymentListener() != null) {
                            GlobalData.getPaymentListener().onComplete();
                        }
                        SingletonLifeCircleManager.disposeAll();
                    }, GlobalData.getStringResource(RS.string.dialog_close_button));
        }
//
        //user have no link bank account so no need to unlink
        else if (paymentInfoHelper.isBankAccountTrans()
                && paymentInfoHelper.bankAccountUnlink()
                && !BankAccountHelper.hasBankAccountOnCache(paymentInfoHelper.getUserId(), paymentInfoHelper.getLinkAccBankCode())) {
            DialogManager.closeProcessDialog();
            DialogManager.showSweetDialog(GlobalData.getMerchantActivity(), SweetAlertDialog.INFO_TYPE,
                    GlobalData.getMerchantActivity().getString(R.string.dialog_title_normal),
                    GlobalData.getMerchantActivity().getString(R.string.zpw_warning_unlink_bankaccount_invalid),
                    pIndex -> {
                        paymentInfoHelper.setResult(PaymentStatus.USER_CLOSE);
                        if (GlobalData.getPaymentListener() != null) {
                            GlobalData.getPaymentListener().onComplete();
                        }
                        SingletonLifeCircleManager.disposeAll();
                    }, GlobalData.getStringResource(RS.string.dialog_close_button));
        }

        //check maintenance link bank account
        else if (paymentInfoHelper.isBankAccountTrans() && bankConfig != null && bankConfig.isBankMaintenence(BankFunctionCode.LINK_BANK_ACCOUNT)) {
            DialogManager.showSweetDialog(GlobalData.getMerchantActivity(), SweetAlertDialog.INFO_TYPE,
                    GlobalData.getMerchantActivity().getString(R.string.dialog_title_normal),
                    bankConfig.getMaintenanceMessage(BankFunctionCode.LINK_BANK_ACCOUNT), pIndex -> {
                        paymentInfoHelper.setResult(PaymentStatus.USER_CLOSE);
                        if (GlobalData.getPaymentListener() != null) {
                            GlobalData.getPaymentListener().onComplete();
                        }
                        SingletonLifeCircleManager.disposeAll();
                    }, GlobalData.getStringResource(RS.string.dialog_close_button));
            return false;
        } else {
            return true;
        }
        return false;
    }

    private static void startGateway(PaymentInfoHelper paymentInfoHelper) {
        Activity pOwner = GlobalData.getMerchantActivity();
        if (pOwner == null || pOwner.isFinishing()) {
            Log.e("startGateway", "merchant activity is null");
            terminateSession(GlobalData.getStringResource(RS.string.zingpaysdk_alert_input_error), PaymentError.DATA_INVALID);
            return;
        }
        Intent intent;
        MiniPmcTransType pmcTransType = null;
        int transtype = paymentInfoHelper.getTranstype();
        //this is link card , go to channel directly
        if (paymentInfoHelper.isCardLinkTrans() || paymentInfoHelper.isBankAccountTrans()) {
            intent = new Intent(GlobalData.getAppContext(), PaymentChannelActivity.class);
            pmcTransType = SDKApplication
                    .getApplicationComponent()
                    .appInfoInteractor()
                    .getPmcTranstype(BuildConfig.ZALOAPP_ID, transtype, paymentInfoHelper.isBankAccountTrans(), null);
            if (pmcTransType != null) {
                intent.putExtra(PMC_CONFIG, pmcTransType);
            }
        } else {
            //intent = new Intent(pOwner, PaymentGatewayActivity.class);
            intent = new Intent(pOwner, ChannelListActivity.class);
        }
        if (pmcTransType == null && intent.getComponent().getClassName().equals(PaymentChannelActivity.class.getName())) {
            terminateSession(GlobalData.getStringResource(RS.string.sdk_config_invalid), PaymentError.DATA_INVALID);
        } else {
            SDKApplication.getApplicationComponent().eventBus().postSticky(paymentInfoHelper);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pOwner.startActivity(intent);
        }
        Log.d("startGateway", intent.getComponent().getShortClassName(), pmcTransType);
    }

    /***
     * show dialog and dispose sdk in error cases
     * @param pMessage
     */
    private static void terminateSession(final String pMessage, @PaymentError int pPayError) {
        DialogManager.closeProcessDialog();
        if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onError(new CError(pPayError, pMessage));
        }
        SingletonLifeCircleManager.disposeAll();
    }
}

package vn.com.zalopay.wallet.controller;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import com.zalopay.ui.widget.dialog.DialogManager;
import com.zalopay.ui.widget.dialog.SweetAlertDialog;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.business.entity.error.CError;
import vn.com.zalopay.wallet.business.fingerprint.IPaymentFingerPrint;
import vn.com.zalopay.wallet.business.objectmanager.SingletonLifeCircleManager;
import vn.com.zalopay.wallet.business.validation.CValidation;
import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.constants.PaymentError;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.datasource.task.SDKReportTask;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.listener.ZPPaymentListener;
import vn.com.zalopay.wallet.utils.ConnectionUtil;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.view.component.activity.BasePaymentActivity;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;
import vn.com.zalopay.wallet.view.component.activity.PaymentGatewayActivity;

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

    /***
     * Pay, app call sdk and set paymentinfo
     * @param owner
     * @param pTransactionType
     * @param info
     * @param listener
     * @param pExtraParams
     */
    public synchronized static void pay(Activity owner, @TransactionType int pTransactionType, ZPWPaymentInfo info, ZPPaymentListener listener, Object... pExtraParams) {
        pay(owner, info, pTransactionType, listener, pExtraParams);
    }

    private synchronized static void pay(final Activity pMerchantActivity, final ZPWPaymentInfo pPaymentInfo, @TransactionType int pTransactionType, final ZPPaymentListener pPaymentListener, Object... pExtraParams) {

        //validate payment info and activity
        if (pMerchantActivity == null || pPaymentInfo == null) {
            if (pPaymentListener != null) {
                pPaymentListener.onError(new CError(PaymentError.COMPONENT_NULL, "Component (activity,payment info) is null"));
            }
            return;
        }
        //set listener and data payment to global static
        try {
            SDKApplication.createPaymentInfoComponent(pPaymentInfo);
            GlobalData.setSDKData(pMerchantActivity, pPaymentListener, pTransactionType);
        } catch (Exception e) {
            onReturnCancel(pMerchantActivity.getResources().getString(R.string.zingpaysdk_alert_input_error), PaymentError.DATA_INVALID);
            return;
        }
        //set fingerprint listener from merchant
        if (pExtraParams != null && pExtraParams.length > 0) {
            for (int i = 0; i < pExtraParams.length; i++) {
                if (pExtraParams[i] instanceof IPaymentFingerPrint) {
                    GlobalData.setIFingerPrint((IPaymentFingerPrint) pExtraParams[i]);
                }
            }
        }
        Log.d("pay", "payment transaction type " + pTransactionType);
        Log.d("pay", "payment info " + GsonUtils.toJsonString(pPaymentInfo));
        //check where context is end?
        try {
            GlobalData.getTransactionType();
            GlobalData.selectBankFunctionByTransactionType();
            Log.d("pay", "transaction type" + GlobalData.getTransactionType());
        } catch (Exception e) {
            Log.e("pay", e);
            onReturnCancel(pMerchantActivity.getResources().getString(R.string.zingpaysdk_alert_context_error), PaymentError.DATA_INVALID);
            return;
        }
        try {
            //check internet connection
            if (!ConnectionUtil.isOnline(pMerchantActivity)) {
                if (GlobalData.getPaymentListener() != null) {
                    GlobalData.getPaymentListener().onError(new CError(PaymentError.NETWORKING_ERROR, GlobalData.getStringResource(RS.string.zingpaysdk_alert_no_connection)));
                }

                SingletonLifeCircleManager.disposeAll();

                return;
            }
            CValidation validation = new CValidation();
            //validate params order info
            String validateMessage = validation.onValidateOrderInfo(pPaymentInfo);
            if (!TextUtils.isEmpty(validateMessage)) {
                onReturnCancel(validateMessage, PaymentError.DATA_INVALID);
                return;
            }
            //validate user
            String validateUser = validation.onValidateUser();

            if (!TextUtils.isEmpty(validateUser)) {
                onReturnCancel(validateUser, PaymentError.DATA_INVALID);
                return;
            }

            //1 zalopay user has only 1 vcb account

            if (GlobalData.isBankAccountLink()
                    && GlobalData.isLinkAccFlow()
                    && BankAccountHelper.hasBankAccountOnCache(GlobalData.getPaymentInfo().userInfo.zaloPayUserId, GlobalData.getPaymentInfo().linkAccInfo.getBankCode())) {
                DialogManager.closeProcessDialog();
                DialogManager.showSweetDialog(GlobalData.getMerchantActivity(), SweetAlertDialog.INFO_TYPE, GlobalData.getMerchantActivity().getString(R.string.dialog_title_normal),
                        GlobalData.getMerchantActivity().getString(R.string.zpw_warning_link_bankaccount_existed), pIndex -> {
                            if (GlobalData.getPaymentListener() != null) {
                                GlobalData.setResultUserClose();
                                GlobalData.getPaymentListener().onComplete(GlobalData.getPaymentResult());
                            }
                            SingletonLifeCircleManager.disposeAll();
                        }, new String[]{GlobalData.getStringResource(RS.string.dialog_close_button)});
                return;
            }
            //user have no link bank account so no need to unlink
            if (GlobalData.isBankAccountLink()
                    && GlobalData.isUnLinkAccFlow()
                    && !BankAccountHelper.hasBankAccountOnCache(GlobalData.getPaymentInfo().userInfo.zaloPayUserId, GlobalData.getPaymentInfo().linkAccInfo.getBankCode())) {
                DialogManager.closeProcessDialog();
                DialogManager.showSweetDialog(GlobalData.getMerchantActivity(), SweetAlertDialog.INFO_TYPE,
                        GlobalData.getMerchantActivity().getString(R.string.dialog_title_normal),
                        GlobalData.getMerchantActivity().getString(R.string.zpw_warning_unlink_bankaccount_invalid),
                        pIndex -> {
                            if (GlobalData.getPaymentListener() != null) {
                                GlobalData.setResultUserClose();
                                GlobalData.getPaymentListener().onComplete(GlobalData.getPaymentResult());
                            }
                            SingletonLifeCircleManager.disposeAll();
                        }, new String[]{GlobalData.getStringResource(RS.string.dialog_close_button)});
                return;
            }

            //check maintenance link bank account
            if (GlobalData.isBankAccountLink() && BankLoader.getInstance().isBankMaintenance(GlobalData.getPaymentInfo().linkAccInfo.getBankCode(), BankFunctionCode.LINK_BANK_ACCOUNT)) {
                DialogManager.showSweetDialog(GlobalData.getMerchantActivity(), SweetAlertDialog.INFO_TYPE,
                        GlobalData.getMerchantActivity().getString(R.string.dialog_title_normal),
                        BankLoader.getInstance().getFormattedBankMaintenaceMessage(), pIndex -> {
                            if (GlobalData.getPaymentListener() != null) {
                                GlobalData.setResultUserClose();
                                GlobalData.getPaymentListener().onComplete(GlobalData.getPaymentResult());
                            }
                            SingletonLifeCircleManager.disposeAll();
                        }, new String[]{GlobalData.getStringResource(RS.string.dialog_close_button)});
                return;
            }
        } catch (Exception e) {
            onReturnCancel(GlobalData.getStringResource(RS.string.zingpaysdk_alert_input_error), PaymentError.DATA_INVALID);
            Log.e("pay", e);
            return;
        }

        startGateway();
    }

    private static void startGateway() {
        Activity pOwner = GlobalData.getMerchantActivity();
        Intent intent = new Intent(pOwner, PaymentGatewayActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pOwner.startActivity(intent);
    }

    /***
     * show dialog and dispose sdk in error cases
     *
     * @param pMessage
     */
    private static void onReturnCancel(final String pMessage, @PaymentError int pPayError) {
        if (pPayError == PaymentError.DATA_INVALID) {
            SDKReportTask.makeReportError(SDKReportTask.INVALID_PAYMENTINFO, GsonUtils.toJsonString(GlobalData.getPaymentInfo()));
        }
        DialogManager.closeProcessDialog();
        if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onError(new CError(pPayError, pMessage));
        }
        SingletonLifeCircleManager.disposeAll();

      /*  DialogManager.showSweetDialog(GlobalData.getMerchantActivity(), SweetAlertDialog.WARNING_TYPE, GlobalData.getMerchantActivity().getString(R.string.dialog_title_warning), pMessage, pIndex -> {
            if (GlobalData.getPaymentListener() != null)
                GlobalData.getPaymentListener().onError(new CError(pPayError, pMessage));

            SingletonLifeCircleManager.disposeAll();
        }, new String[]{GlobalData.getStringResource(RS.string.dialog_close_button)});*/
    }
}

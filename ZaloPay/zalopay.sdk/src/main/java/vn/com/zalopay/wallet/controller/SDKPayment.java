package vn.com.zalopay.wallet.controller;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentOption;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.business.entity.enumeration.EBankFunction;
import vn.com.zalopay.wallet.business.entity.enumeration.EPayError;
import vn.com.zalopay.wallet.business.entity.enumeration.EPaymentChannel;
import vn.com.zalopay.wallet.business.entity.error.CError;
import vn.com.zalopay.wallet.business.fingerprint.IPaymentFingerPrint;
import vn.com.zalopay.wallet.business.objectmanager.SingletonLifeCircleManager;
import vn.com.zalopay.wallet.business.validation.CValidation;
import vn.com.zalopay.wallet.datasource.request.SDKReport;
import vn.com.zalopay.wallet.datasource.request.SaveCard;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.listener.ZPPaymentListener;
import vn.com.zalopay.wallet.listener.ZPWOnSweetDialogListener;
import vn.com.zalopay.wallet.listener.ZPWSaveMapCardListener;
import vn.com.zalopay.wallet.utils.ConnectionUtil;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.utils.StringUtil;
import vn.com.zalopay.wallet.view.component.activity.BasePaymentActivity;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;
import vn.com.zalopay.wallet.view.component.activity.PaymentGatewayActivity;
import vn.com.zalopay.wallet.view.dialog.DialogManager;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/***
 * payment controller class
 */
public class SDKPayment {
    /**
     * Save card when pay or mapcard finish
     *
     * @param pPaymentInfo payment info
     * @param pListener    call back
     */
    public synchronized static void saveCard(ZPWPaymentInfo pPaymentInfo, ZPWSaveMapCardListener pListener) {
        try {
            if (SDKApplication.getZaloPayContext() == null || pPaymentInfo == null || TextUtils.isEmpty(pPaymentInfo.walletTransID)) {
                if (pListener != null)
                    pListener.onError("Dữ liệu không hợp lệ");
                return;
            }

            GlobalData.initApplication(pPaymentInfo, pListener);

            SaveCard saveCreditCardTask = new SaveCard(null, pPaymentInfo.walletTransID);
            saveCreditCardTask.setOnSaveCardListener(pListener);
            saveCreditCardTask.makeRequest();

        } catch (Exception e) {
            Log.e("saveCardMap", e != null ? e.getMessage() : "error");

            if (pListener != null)
                pListener.onError(null);

            return;
        }
    }

    /***
     * merchant check whether
     * use is opening sdk for payment
     *
     * @return
     */
    public synchronized static boolean isOpenSdk() {
        return GlobalData.isUserInSDK();
    }

    /***
     * merchant need to call this to check whether
     * can close sdk right away before calling closeSdk();
     *
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
     * Pay, app call sdk and set paymentinfo
     *
     * @param owner             ownerActivity
     * @param paymentMethodType paymentMethodType
     * @param info              pay ment info
     * @param listener          call back
     */
    public synchronized static void pay(Activity owner, EPaymentChannel paymentMethodType, ZPWPaymentInfo info, ZPPaymentListener listener, Object... pExtraParams) {

        ZPPaymentOption option = new ZPPaymentOption(paymentMethodType);
        pay(owner, info, option, listener, pExtraParams);
    }

    private synchronized static void pay(final Activity pMerchantActivity, final ZPWPaymentInfo info, final ZPPaymentOption option, final ZPPaymentListener listener, Object... pExtraParams) {

        //validate payment info and activity
        if (pMerchantActivity == null || info == null) {
            if (listener != null) {
                listener.onError(new CError(EPayError.COMPONENT_NULL, "Component (activity,payment info) is null"));
            }
            return;
        }

        //set listener and data payment to global static
        try {
            GlobalData.setSDKData(pMerchantActivity, listener, info, option);
        } catch (Exception e) {
            onReturnCancel(pMerchantActivity.getResources().getString(R.string.zingpaysdk_alert_input_error), EPayError.DATA_INVALID);

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

        Log.d("pay", "===info payment===" + GsonUtils.toJsonString(info));

        //check where context is end?
        try {
            SDKApplication.getZaloPayContext();

            GlobalData.getTransactionType();

            Log.d("pay", "===transtype: ===" + GlobalData.getTransactionType().toString());

        } catch (Exception e) {

            Log.e("pay", e);

            onReturnCancel(pMerchantActivity.getResources().getString(R.string.zingpaysdk_alert_context_error), EPayError.DATA_INVALID);

            return;
        }


        try {
            //check internet connection
            if (!ConnectionUtil.isOnline(pMerchantActivity)) {
                if (GlobalData.getPaymentListener() != null) {
                    GlobalData.getPaymentListener().onError(new CError(EPayError.NETWORKING_ERROR, GlobalData.getStringResource(RS.string.zingpaysdk_alert_no_connection)));
                }

                SingletonLifeCircleManager.disposeAll();

                return;
            }

            CValidation validation = new CValidation();
            //validate params order info
            String validateMessage = validation.onValidateOrderInfo(info);

            if (!TextUtils.isEmpty(validateMessage)) {
                onReturnCancel(validateMessage, EPayError.DATA_INVALID);
                return;
            }

            //validate user
            String validateUser = validation.onValidateUser();

            if (!TextUtils.isEmpty(validateUser)) {
                onReturnCancel(validateUser, EPayError.DATA_INVALID);
                return;
            }

            //1 zalopay user has only 1 vcb account

            if (GlobalData.isLinkAccChannel()
                    && GlobalData.isLinkAccFlow()
                    && BankAccountHelper.hasBankAccountOnCache(GlobalData.getPaymentInfo().userInfo.zaloPayUserId, GlobalData.getPaymentInfo().linkAccInfo.getBankCode())) {
                DialogManager.closeProcessDialog();
                DialogManager.showSweetDialog(GlobalData.getMerchantActivity(), SweetAlertDialog.INFO_TYPE, GlobalData.getMerchantActivity().getString(R.string.dialog_title_normal),
                        GlobalData.getMerchantActivity().getString(R.string.zpw_warning_link_bankaccount_existed), new ZPWOnSweetDialogListener() {
                            @Override
                            public void onClickDiaLog(int pIndex) {
                                if (GlobalData.getPaymentListener() != null) {
                                    GlobalData.setResultUserClose();
                                    GlobalData.getPaymentListener().onComplete(GlobalData.getPaymentResult());
                                }
                                SingletonLifeCircleManager.disposeAll();
                            }
                        }, new String[]{GlobalData.getStringResource(RS.string.dialog_close_button)});
                return;
            }
            //user have no link bank account so no need to unlink
            if (GlobalData.isLinkAccChannel()
                    && GlobalData.isUnLinkAccFlow()
                    && !BankAccountHelper.hasBankAccountOnCache(GlobalData.getPaymentInfo().userInfo.zaloPayUserId, GlobalData.getPaymentInfo().linkAccInfo.getBankCode())) {
                DialogManager.closeProcessDialog();
                DialogManager.showSweetDialog(GlobalData.getMerchantActivity(), SweetAlertDialog.INFO_TYPE,
                        GlobalData.getMerchantActivity().getString(R.string.dialog_title_normal),
                        GlobalData.getMerchantActivity().getString(R.string.zpw_warning_unlink_bankaccount_invalid),
                        new ZPWOnSweetDialogListener() {
                            @Override
                            public void onClickDiaLog(int pIndex) {
                                if (GlobalData.getPaymentListener() != null) {
                                    GlobalData.setResultUserClose();
                                    GlobalData.getPaymentListener().onComplete(GlobalData.getPaymentResult());
                                }
                                SingletonLifeCircleManager.disposeAll();
                            }
                        }, new String[]{GlobalData.getStringResource(RS.string.dialog_close_button)});
                return;
            }

            //check maintenance link bank account
            if (GlobalData.isLinkAccChannel() && BankLoader.getInstance().isBankMaintenance(GlobalData.getPaymentInfo().linkAccInfo.getBankCode(), EBankFunction.LINK_BANK_ACCOUNT)) {
                DialogManager.showSweetDialog(GlobalData.getMerchantActivity(), SweetAlertDialog.INFO_TYPE,
                        GlobalData.getMerchantActivity().getString(R.string.dialog_title_normal),
                        StringUtil.getFormattedBankMaintenaceMessage(), new ZPWOnSweetDialogListener() {
                            @Override
                            public void onClickDiaLog(int pIndex) {
                                if (GlobalData.getPaymentListener() != null) {
                                    GlobalData.setResultUserClose();
                                    GlobalData.getPaymentListener().onComplete(GlobalData.getPaymentResult());
                                }
                                SingletonLifeCircleManager.disposeAll();
                            }
                        }, new String[]{GlobalData.getStringResource(RS.string.dialog_close_button)});
                return;
            }
        } catch (Exception e) {
            onReturnCancel(GlobalData.getStringResource(RS.string.zingpaysdk_alert_input_error), EPayError.DATA_INVALID);
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
    private static void onReturnCancel(final String pMessage, final EPayError pPayError) {
        if (pPayError == EPayError.DATA_INVALID) {
            SDKReport.makeReportError(SDKReport.INVALID_PAYMENTINFO, GsonUtils.toJsonString(GlobalData.getPaymentInfo()));
        }
        DialogManager.closeProcessDialog();
        DialogManager.showSweetDialog(GlobalData.getMerchantActivity(), SweetAlertDialog.WARNING_TYPE, GlobalData.getMerchantActivity().getString(R.string.dialog_title_warning), pMessage, new ZPWOnSweetDialogListener() {
            @Override
            public void onClickDiaLog(int pIndex) {
                if (GlobalData.getPaymentListener() != null)
                    GlobalData.getPaymentListener().onError(new CError(pPayError, pMessage));

                SingletonLifeCircleManager.disposeAll();
            }
        }, new String[]{GlobalData.getStringResource(RS.string.dialog_close_button)});
    }
}

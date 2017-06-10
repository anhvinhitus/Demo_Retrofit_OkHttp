package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.text.TextUtils;

import com.zalopay.ui.widget.dialog.SweetAlertDialog;

import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.PhoneUtil;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.pw.PaymentWrapper;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.paymentinfo.IBuilder;

/**
 * Created by longlv on 5/29/17.
 * Detect LinkCard/LinkAccount
 */

abstract class AbstractBankPresenter<View> extends AbstractPresenter<View> {

    abstract Activity getActivity();

    abstract User getUser();

    abstract PaymentWrapper getPaymentWrapper();

    abstract void onAddBankCardSuccess(MapCard bankCard);

    abstract void onAddBankAccountSuccess(BankAccount bankAccount);

    abstract void onUnLinkBankAccountSuccess(BankAccount bankAccount);

    void onResponseSuccessFromSDK(IBuilder builder) {
        if (builder == null) {
            Timber.d("PaymentSDK response success but paymentInfo builder null");
            return;
        }
        if (builder.isLinkAccount()) {
            onAddBankAccountSuccess((BankAccount) builder.getMapBank());
        } else if (builder.isUnLinkAccount()) {
            onUnLinkBankAccountSuccess((BankAccount) builder.getMapBank());
        } else if (builder.getMapBank() != null) {
            onAddBankCardSuccess((MapCard) builder.getMapBank());
        }
    }

    void linkAccount(String cardCode) {
        List<BankAccount> mapCardLis = CShareDataWrapper.getMapBankAccountList(getUser());
        if (checkLinkedBankAccount(mapCardLis, cardCode)) {
            showVCBWarningDialog();
        } else {
            showVCBConfirmDialog(cardCode);
        }
    }

    private void showVCBWarningDialog() {
        if (mView == null) return;
        SweetAlertDialog dialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE, R.style.alert_dialog);
        dialog.setTitleText(getActivity().getString(R.string.notification));
        dialog.setContentText(getActivity().getString(R.string.link_account_vcb_exist));
        dialog.setConfirmText(getActivity().getString(R.string.txt_close));
        dialog.setConfirmClickListener((SweetAlertDialog sweetAlertDialog) -> {
            dialog.dismiss();
        });
        dialog.show();
    }

    private void showVCBConfirmDialog(String cardCode) {
        if (mView == null) return;
        SweetAlertDialog dialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE, R.style.alert_dialog);

        dialog.setTitleText(getActivity().getString(R.string.notification));
        dialog.setCancelText(getActivity().getString(R.string.txt_cancel));
        dialog.setContentText(getVCBWarningMessage());
        dialog.setConfirmText(getActivity().getString(R.string.accept));
        dialog.setConfirmClickListener((SweetAlertDialog sweetAlertDialog) -> {
            getPaymentWrapper().linkAccount(getActivity(), cardCode);
//            getPaymentWrapper().linkAccount(getActivity(), "ZPVCB");
            dialog.dismiss();
        });
        dialog.show();
    }

    //Just to note, though, the Java compiler will automatically convert.
    //Ref: https://stackoverflow.com/questions/4965513/stringbuilder-vs-string-considering-replace
    private String getVCBWarningMessage() {
        return String.format(getActivity().getString(R.string.link_account_empty_bank_support_phone_require_hint),
                "<b>" + PhoneUtil.formatPhoneNumberWithDot(getUser().phonenumber) + "</b>") +
                "<br><br>" +
                getActivity().getString(R.string.link_account_empty_bank_support_balance_require_hint);
    }

    boolean checkLinkedBankAccount(List<BankAccount> listBankAccount, String bankCode) {
        if (Lists.isEmptyOrNull(listBankAccount)) {
            return false;
        }
        for (BankAccount bankAccount : listBankAccount) {
            if (bankAccount == null || TextUtils.isEmpty(bankAccount.bankcode)) {
                continue;
            }
            if (bankAccount.bankcode.equalsIgnoreCase(bankCode)) {
                return true;
            }
        }
        return false;
    }
}

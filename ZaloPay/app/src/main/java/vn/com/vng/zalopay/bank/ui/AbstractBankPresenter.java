package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.text.TextUtils;

import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.PhoneUtil;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by longlv on 5/29/17.
 * Detect LinkCard/LinkAccount
 */

abstract class AbstractBankPresenter<View> extends AbstractPresenter<View> {

    abstract Activity getActivity();

    abstract User getUser();

    abstract PaymentWrapper getPaymentWrapper();

    abstract void onAddBankCardSuccess(DMappedCard bankCard);

    abstract void onAddBankAccountSuccess(DBankAccount bankAccount);

    abstract void onUnLinkBankAccountSuccess(DBankAccount bankAccount);

    void onResponseSuccessFromSDK(ZPPaymentResult zpPaymentResult) {
        ZPWPaymentInfo paymentInfo = zpPaymentResult.paymentInfo;
        if (paymentInfo == null) {
            Timber.d("PaymentSDK response success but paymentInfo null");
            return;
        }

        // TODO: 5/29/17 - longlv: Hiện chưa phân tách rõ ràng được giữa LinkCard & LinkAcc/UnLinkAcc
        //Nếu linkAccInfo != null && paymentInfo.mapBank != null -> LinkAcc/UnLinkAcc
        //Nếu linkAccInfo == null && paymentInfo.mapBank != null -> LinkCard
        if (paymentInfo.linkAccInfo != null) {
            if (paymentInfo.linkAccInfo.isLinkAcc()) {
                onAddBankAccountSuccess((DBankAccount) paymentInfo.mapBank);
            } else if (paymentInfo.linkAccInfo.isUnlinkAcc()) {
                onUnLinkBankAccountSuccess((DBankAccount) paymentInfo.mapBank);
            }
        } else if (paymentInfo.mapBank != null) {
            onAddBankCardSuccess((DMappedCard) paymentInfo.mapBank);
        }
    }

    void linkAccount(String cardCode) {
        List<DBankAccount> mapCardLis = CShareDataWrapper.getMapBankAccountList(getUser());
        if (checkLinkedBankAccount(mapCardLis, cardCode)) {
            getPaymentWrapper().linkAccount(getActivity(), cardCode);
        } else {
            showVCBWarningDialog();
        }
    }

    private void showVCBWarningDialog() {
        if (mView == null) return;
        SweetAlertDialog dialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE, R.style.alert_dialog);

        dialog.setTitleText(getActivity().getString(R.string.notification));
        dialog.setCancelText(getActivity().getString(R.string.txt_cancel));
        dialog.setContentText(getVCBWarningMessage());
        dialog.setConfirmText(getActivity().getString(R.string.accept));
        dialog.setConfirmClickListener((SweetAlertDialog sweetAlertDialog) -> {
            getPaymentWrapper().linkAccount(getActivity(), "ZPVCB");
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

    boolean checkLinkedBankAccount(List<DBankAccount> listBankAccount, String bankCode) {
        if (Lists.isEmptyOrNull(listBankAccount)) {
            return false;
        }
        for (DBankAccount bankAccount : listBankAccount) {
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

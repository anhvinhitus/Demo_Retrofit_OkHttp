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
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
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

        BaseMap baseMap = builder.getMapBank();

        if (baseMap instanceof BankAccount) {

            if (builder.isLinkAccount()) {
                onAddBankAccountSuccess((BankAccount) baseMap);
            } else if (builder.isUnLinkAccount()) {
                onUnLinkBankAccountSuccess((BankAccount) baseMap);
            }

        } else if (baseMap instanceof MapCard) {
            onAddBankCardSuccess((MapCard) baseMap);
        } else {
            Timber.w("Response success from SDK : BaseMap - other type: %s", baseMap);
        }
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

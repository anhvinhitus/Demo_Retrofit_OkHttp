package vn.com.vng.zalopay.bank.ui;

import timber.log.Timber;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;

/**
 * Created by longlv on 5/29/17.
 * Detect LinkCard/LinkAccount
 */

abstract class AbstractBankPresenter<View> extends AbstractPresenter<View> {

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
}

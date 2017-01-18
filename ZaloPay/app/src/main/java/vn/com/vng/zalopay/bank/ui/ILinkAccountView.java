package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;

import java.util.List;

import vn.com.vng.zalopay.bank.models.BankAccount;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;

/**
 * Created by longlv on 1/17/17.
 *
 */
interface ILinkAccountView extends ILoadDataView {

    Activity getActivity();

    void setData(List<BankAccount> list);

    void removeLinkAccount(BankAccount bankAccount);

    void onAddAccountSuccess(DMappedCard mappedCreditCard);
}

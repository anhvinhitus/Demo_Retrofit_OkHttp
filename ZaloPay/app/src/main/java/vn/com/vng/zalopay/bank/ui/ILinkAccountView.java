package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import vn.com.vng.zalopay.bank.models.BankAccount;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;

/**
 * Created by longlv on 1/17/17.
 *
 */
interface ILinkAccountView extends ILoadDataView {

    Activity getActivity();

    void refreshLinkedAccount(List<BankAccount> bankAccounts);

    void insertData(BankAccount bankAccounts);

    void removeData(BankAccount bankAccounts);

    void showListBankDialog(ArrayList<ZPCard> cardSupportList);

    void showRetryDialog(String message, ZPWOnEventConfirmDialogListener listener);

    void onUpdateVersion(boolean forceUpdate, String latestVersion, String message);

    boolean getUserVisibleHint();
}

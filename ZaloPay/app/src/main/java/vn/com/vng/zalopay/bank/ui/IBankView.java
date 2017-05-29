package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.support.v4.app.Fragment;

import java.util.List;

import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;

/**
 * Created by datnt10 on 5/25/17.
 * Interface of BankPresenter & BankFragment
 */

interface IBankView extends ILoadDataView {
    Activity getActivity();

    Fragment getFragment();

    void setListLinkedBank(List<DBaseMap> linkedBankList);

    void refreshLinkedBankList();

    void removeLinkedBank(DBaseMap mapBank);

    void showNotificationDialog(String message);

    void showConfirmDialogAfterLinkBank(String message);

    void onAddBankSuccess(DBaseMap bankInfo);
}

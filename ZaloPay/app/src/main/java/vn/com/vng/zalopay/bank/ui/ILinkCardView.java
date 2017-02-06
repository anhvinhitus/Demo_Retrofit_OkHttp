package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import vn.com.vng.zalopay.domain.model.BankCard;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;

/**
 * Created by AnhHieu on 5/11/16.
 *
 */
interface ILinkCardView extends ILoadDataView {

    Activity getActivity();

    void setData(List<BankCard> bankCards);

    void updateData(BankCard bankCard);

    void removeData(BankCard bankCard);

    void onAddCardSuccess(DBaseMap card);

    void showWarningView(String error);

    boolean getUserVisibleHint();

    void showRetryDialog(String message, ZPWOnEventConfirmDialogListener listener);

    void onUpdateVersion(boolean forceUpdate, String latestVersion, String message);

    void showListBankSupportDialog(ArrayList<ZPCard> cards);
}

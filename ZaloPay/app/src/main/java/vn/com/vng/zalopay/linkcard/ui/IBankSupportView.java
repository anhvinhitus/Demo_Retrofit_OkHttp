package vn.com.vng.zalopay.linkcard.ui;

import android.app.Activity;

import java.util.List;

import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;

/**
 * Created by longlv on 10/22/16.
 * *
 */
interface IBankSupportView extends ILoadDataView {

    Activity getActivity();
    void onEventUpdateVersion(boolean forceUpdate, String latestVersion, String message);
    void refreshBankSupports(List<ZPCard> cardSupportList);
    void showRetryDialog(String message, ZPWOnEventConfirmDialogListener listener);
}

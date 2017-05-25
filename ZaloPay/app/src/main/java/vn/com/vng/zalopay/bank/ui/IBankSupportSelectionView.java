package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.content.Context;

import java.util.List;

import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;

/**
 * Created by datnt10 on 5/25/17.
 */

public interface IBankSupportSelectionView {
    Activity getActivity();

    Context getContext();

    void fetchListBank(List<ZPCard> cardSupportList);

    void showRetryDialog(String message, ZPWOnEventConfirmDialogListener listener);
}

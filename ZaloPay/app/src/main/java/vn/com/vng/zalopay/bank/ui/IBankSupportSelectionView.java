package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.content.Context;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;

import java.util.List;

import vn.com.zalopay.wallet.merchant.entities.ZPBank;

/**
 * Created by datnt10 on 5/25/17.
 */

interface IBankSupportSelectionView {
    Activity getActivity();

    Context getContext();

    void setData(List<ZPBank> banks);

    void showLoading();

    void hideLoading();

    void showError(String message);

    void showMessageDialog(String message, ZPWOnEventDialogListener closeDialogListener);

    void showDialogThenClose(String content, String title, int dialogType);

    void showConfirmDialog(String pMessage,
                           String pOKButton,
                           String pCancelButton,
                           ZPWOnEventConfirmDialogListener callback);

    void showNetworkErrorDialog();
}

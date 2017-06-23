package vn.com.zalopay.wallet.ui;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;

/**
 * Created by chucvv on 6/12/17.
 */

public interface IContract {
    void showLoading(String pTitle);

    void hideLoading();

    void showError(String pMessage);

    void showInfoDialog(String pMessage);

    void showInfoDialog(String pMessage, ZPWOnEventDialogListener zpwOnEventDialogListener);

    void terminate();

    void callbackThenterminate();
}

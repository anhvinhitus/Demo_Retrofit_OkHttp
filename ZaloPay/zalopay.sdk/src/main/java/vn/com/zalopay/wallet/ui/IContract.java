package vn.com.zalopay.wallet.ui;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;

import vn.com.zalopay.wallet.listener.ZPWPaymentOpenNetworkingDialogListener;
import vn.com.zalopay.wallet.listener.onCloseSnackBar;

/**
 * Created by chucvv on 6/12/17.
 */

public interface IContract {
    void showLoading(String pTitle);

    void hideLoading();

    void showError(String pMessage);

    void showInfoDialog(String pMessage);

    void showInfoDialog(String pMessage, ZPWOnEventDialogListener zpwOnEventDialogListener);

    void showUpdateLevelDialog(String message, String btnCloseText, ZPWOnEventConfirmDialogListener pListener);

    void showRetryDialog(String pMessage, ZPWOnEventConfirmDialogListener pListener);

    void showOpenSettingNetwokingDialog(ZPWPaymentOpenNetworkingDialogListener pListener);

    void showSnackBar(String pMessage, String pActionMessage, int pDuration, onCloseSnackBar pOnCloseListener);

    void terminate();

    void callbackThenTerminate();
}

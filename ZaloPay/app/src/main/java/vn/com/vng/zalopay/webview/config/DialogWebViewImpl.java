package vn.com.vng.zalopay.webview.config;

import android.app.Activity;

import vn.com.vng.zalopay.webview.interfaces.IDialogListener;
import vn.com.vng.zalopay.webview.interfaces.ITimeoutLoadingListener;
import vn.com.vng.zalopay.webview.interfaces.IDialog;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.listener.ZPWOnEventDialogListener;
import vn.com.zalopay.wallet.listener.ZPWOnProgressDialogTimeoutListener;
import vn.com.zalopay.wallet.view.dialog.DialogManager;

/**
 * Created by admin on 8/30/16.
 *
 */
public class DialogWebViewImpl implements IDialog {
    @Override
    public void showInfoDialog(Activity pActivity, String pMessage, String pButtonText, int pDialogType, final IDialogListener pListener) {
        DialogManager.showSweetDialogCustom(pActivity, pMessage, pButtonText, pDialogType, new ZPWOnEventDialogListener() {
            @Override
            public void onOKevent() {
                if (pListener != null)
                    pListener.onClose();
            }
        });
    }

    @Override
    public void showLoadingDialog(Activity pActivity, final ITimeoutLoadingListener pListener) {
        DialogManager.showProcessDialog(pActivity, new ZPWOnProgressDialogTimeoutListener() {
            @Override
            public void onProgressTimeout() {
                if (pListener != null)
                    pListener.onTimeoutLoading();
            }
        });
    }

    @Override
    public void showConfirmDialog(Activity pActivity, String pMessage, String pButtonTextLeft, String pButtonTextRight, final IDialogListener pListener) {
        showConfirmDialog(pActivity, pMessage, pButtonTextLeft, pButtonTextRight, pListener, null);
    }

    @Override
    public void showConfirmDialog(Activity pActivity, String pMessage, String pButtonTextLeft, String pButtonTextRight, final IDialogListener btnLeftListener, final IDialogListener btnRightListener) {
        DialogManager.showSweetDialogConfirm(pActivity, pMessage, pButtonTextLeft, pButtonTextRight, new ZPWOnEventConfirmDialogListener() {
            @Override
            public void onCancelEvent() {
                if (btnLeftListener != null) {
                    btnLeftListener.onClose();
                }
            }

            @Override
            public void onOKevent() {
                if (btnRightListener != null) {
                    btnRightListener.onClose();
                }
            }
        });
    }

    @Override
    public void hideLoadingDialog() {
        DialogManager.closeProcessDialog();
    }
}

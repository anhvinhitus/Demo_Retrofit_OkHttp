package vn.com.vng.zalopay.webview.interfaces;

import android.app.Activity;

/**
 * Created by admin on 8/30/16.
 *
 */
public interface IDialog {
    void showInfoDialog(Activity pActivity, String pMessage, String pButtonText, int pDialogType, IDialogListener pListener);

    void showLoadingDialog(Activity pActivity, ITimeoutLoadingListener pListener);

    void showConfirmDialog(Activity pActivity, String pMessage, String pButtonTextLeft, String pButtonTextRight, IDialogListener pListener);

    void showConfirmDialog(Activity pActivity, String pMessage, String pButtonTextLeft, String pButtonTextRight, IDialogListener btnLeftListener, IDialogListener btnRightListener);

    void hideLoadingDialog();
}

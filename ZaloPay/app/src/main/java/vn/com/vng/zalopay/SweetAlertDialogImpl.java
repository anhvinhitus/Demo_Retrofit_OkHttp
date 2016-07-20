package vn.com.vng.zalopay;

import android.content.Context;

import vn.com.vng.zalopay.mdl.sweetalertdialog.ISweetAlertDialog;
import vn.com.vng.zalopay.mdl.sweetalertdialog.ISweetAlertDialogListener;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by longlv on 20/07/2016.
 * Implement ISweetAlertDialog
 */
public class SweetAlertDialogImpl implements ISweetAlertDialog {
    @Override
    public void showWarningAlertDialog(Context context, String contentText, String cancelText, final ISweetAlertDialogListener cancelClick, String ConfirmText, final ISweetAlertDialogListener confirmClick) {
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setContentText(contentText)
                .setCancelText(cancelText)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        if (cancelClick != null) {
                            cancelClick.onClick(sweetAlertDialog);
                        }
                    }
                })
                .setConfirmText(ConfirmText)
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        if (confirmClick != null) {
                            confirmClick.onClick(sweetAlertDialog);
                        }
                    }
                })
                .show();
    }
}

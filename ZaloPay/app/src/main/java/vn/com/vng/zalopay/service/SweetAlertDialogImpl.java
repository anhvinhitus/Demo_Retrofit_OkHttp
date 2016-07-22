package vn.com.vng.zalopay.service;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;

import vn.com.vng.zalopay.mdl.AlertDialogProvider;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by longlv on 20/07/2016.
 * Implement AlertDialogProvider
 */
public class SweetAlertDialogImpl implements AlertDialogProvider {

    private SweetAlertDialog sweetAlertDialog;

    @Override
    public void showLoading(Context context) {
        sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        sweetAlertDialog.show();
    }

    @Override
    public void hideLoading() {
        if (sweetAlertDialog == null) {
            return;
        }
        sweetAlertDialog.dismiss();
        sweetAlertDialog = null;
    }

    @Override
    public void showWarningAlertDialog(Context context, String contentText, String cancelText, String confirmText, final DialogInterface.OnCancelListener cancelClick, final DialogInterface.OnClickListener confirmClick) {
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setContentText(contentText)
                .setCancelText(cancelText)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        if (cancelClick != null) {
                            cancelClick.onCancel(sweetAlertDialog);
                        }
                    }
                })
                .setConfirmText(confirmText)
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        if (confirmClick != null) {
                            confirmClick.onClick(sweetAlertDialog, 0);
                        }
                    }
                })
                .show();
    }
}

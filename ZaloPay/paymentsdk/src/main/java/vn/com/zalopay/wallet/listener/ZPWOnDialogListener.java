package vn.com.zalopay.wallet.listener;

import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

public interface ZPWOnDialogListener {
    void onCloseDialog(SweetAlertDialog sweetAlertDialog, int pIndexClick);
}

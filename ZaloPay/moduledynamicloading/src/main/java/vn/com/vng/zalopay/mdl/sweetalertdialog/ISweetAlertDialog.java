package vn.com.vng.zalopay.mdl.sweetalertdialog;

import android.content.Context;

/**
 * Created by longlv on 20/07/2016.
 * Define methods using to show alert dialog
 */
public interface ISweetAlertDialog {

    void showWarningAlertDialog(Context context, String contentText, String cancelText, ISweetAlertDialogListener cancelClick, String ConfirmText, ISweetAlertDialogListener confirmClick);
}

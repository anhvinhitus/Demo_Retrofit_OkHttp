package vn.com.vng.zalopay.utils;

import android.app.Activity;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.react.Helpers;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.listener.ZPWOnEventDialogListener;
import vn.com.zalopay.wallet.listener.ZPWOnEventUpdateListener;
import vn.com.zalopay.wallet.listener.ZPWOnProgressDialogTimeoutListener;
import vn.com.zalopay.wallet.listener.ZPWOnSweetDialogListener;
import vn.com.zalopay.wallet.view.dialog.DialogManager;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by longlv on 12/9/16.
 * Wrap DialogManager of WalletSDK
 */

public class DialogHelper {

    public static void showNetworkErrorDialog(Activity activity,
                                              ZPWOnSweetDialogListener listener) {
        if (activity == null) {
            return;
        }
        DialogManager.showDialog(activity,
                activity.getString(R.string.txt_warning),
                activity.getString(R.string.exception_no_connection_try_again),
                R.drawable.ic_no_internet,
                listener,
                activity.getString(R.string.txt_close));
    }

    /*public static void showErrorDialog(Activity activity, String message) {
        DialogManager.showSweetDialogCustom(activity,
                message,
                activity.getString(R.string.txt_close),
                SweetAlertDialog.ERROR_TYPE,
                null);
    }

    public static void showErrorDialog(Activity activity,
                                       String message,
                                       String cancelText,
                                       ZPWOnEventDialogListener cancelListener) {
        DialogManager.showSweetDialogCustom(activity,
                message,
                cancelText,
                SweetAlertDialog.ERROR_TYPE,
                cancelListener);
    }*/

    public static void showErrorDialog(Activity activity,
                                       String title,
                                       String message,
                                       String cancelText,
                                       ZPWOnEventDialogListener cancelListener) {
        DialogManager.showSweetDialogCustom(activity,
                message,
                cancelText,
                title,
                SweetAlertDialog.ERROR_TYPE,
                cancelListener);
    }

    public static void showWarningDialog(Activity activity,
                                         String message,
                                         ZPWOnEventDialogListener cancelListener) {
        showWarningDialog(activity,
                message,
                activity.getString(R.string.txt_close),
                cancelListener);
    }

    public static void showWarningDialog(Activity activity,
                                         String message,
                                         String cancelBtnText,
                                         ZPWOnEventDialogListener cancelListener) {
        DialogManager.showSweetDialogCustom(activity,
                message,
                cancelBtnText,
                SweetAlertDialog.WARNING_TYPE,
                cancelListener);
    }

    public static void showNotificationDialog(Activity activity,
                                              String message) {
        showNotificationDialog(activity,
                message,
                null);
    }

    public static void showNotificationDialog(Activity activity,
                                              String message,
                                              ZPWOnEventDialogListener cancelListener) {
        showNotificationDialog(activity,
                message,
                activity.getString(R.string.txt_close),
                cancelListener);
    }

    public static void showNotificationDialog(Activity activity,
                                              String message,
                                              String btnCancel,
                                              ZPWOnEventDialogListener cancelListener) {
        if (activity == null) {
            return;
        }
        DialogManager.showSweetDialogCustom(activity,
                message,
                btnCancel,
                SweetAlertDialog.NORMAL_TYPE,
                cancelListener);
    }

    public static void showRetryDialog(Activity activity,
                                       String retryMessage,
                                       ZPWOnEventConfirmDialogListener retryListener) {
        DialogManager.showSweetDialogRetry(activity,
                retryMessage,
                retryListener);
    }

    public static void showConfirmDialog(Activity activity,
                                         String pMessage,
                                         String pOKButton,
                                         String pCancelButton,
                                         ZPWOnEventConfirmDialogListener callback) {
        DialogManager.showSweetDialogConfirm(activity,
                pMessage,
                pOKButton,
                pCancelButton,
                callback);
    }

    public static void showSuccessDialog(Activity activity,
                                         String message,
                                         ZPWOnEventDialogListener listener) {
        if (activity == null) {
            return;
        }
        DialogManager.showSweetDialogCustom(activity,
                message,
                activity.getString(R.string.txt_close),
                SweetAlertDialog.SUCCESS_TYPE,
                listener);
    }

    public static void showCustomDialog(Activity activity,
                                        String message,
                                        String cancelBtnText,
                                        int dialogType,
                                        ZPWOnEventDialogListener listener) {
        DialogManager.showSweetDialogCustom(activity, message, cancelBtnText, dialogType, listener);
    }

    static void showSweetDialogUpdate(Activity activity,
                                      String contentText,
                                      String newVersion,
                                      ZPWOnEventUpdateListener listener,
                                      boolean forceUpdate) {
        if (activity == null) {
            return;
        }
        if (forceUpdate) {
            DialogManager.showSweetDialogUpdate(activity,
                    contentText,
                    newVersion,
                    activity.getString(R.string.btn_update),
                    null,
                    listener);
        } else {
            DialogManager.showSweetDialogUpdate(activity,
                    contentText,
                    newVersion,
                    activity.getString(R.string.btn_update),
                    activity.getString(R.string.btn_cancel),
                    listener);
        }
    }

    public static void showNoticeDialog(Activity activity,
                                        String message,
                                        String btnConfirm,
                                        String btnCancel,
                                        ZPWOnEventConfirmDialogListener listener) {
        DialogManager.showSweetDialogOptionNotice(activity, message, btnConfirm, btnCancel, listener);
    }

    public static void showCustomDialog(Activity activity,
                                        int dialogType,
                                        String title,
                                        String message,
                                        ZPWOnSweetDialogListener listener,
                                        String[] btnNames) {
        DialogManager.showDialog(activity,
                dialogType,
                title,
                message,
                listener,
                btnNames);
    }

    public static void showLoading(Activity activity,
                                   ZPWOnProgressDialogTimeoutListener listener) {
        DialogManager.showProcessDialog(activity, listener);
    }

    public static void hideLoading() {
        DialogManager.closeProcessDialog();
    }
}

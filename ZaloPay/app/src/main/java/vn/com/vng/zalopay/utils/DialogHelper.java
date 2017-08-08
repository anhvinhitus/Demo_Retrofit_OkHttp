package vn.com.vng.zalopay.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.KeyEvent;

import com.zalopay.ui.widget.dialog.DialogManager;
import com.zalopay.ui.widget.dialog.SweetAlertDialog;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventUpdateListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnSweetDialogListener;

import timber.log.Timber;
import vn.com.vng.zalopay.R;


/**
 * Created by longlv on 12/9/16.
 * Wrap DialogManager of WalletSDK
 */

public class DialogHelper {
    private DialogHelper() {
        // private constructor for utils class
    }

    private static Dialog mProgressDialog;

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

    public static void showNetworkErrorDialog(Activity activity,
                                              ZPWOnSweetDialogListener listener) {
        if (activity == null) {
            return;
        }
        DialogManager.showDrawableDialog(activity,
                activity.getString(R.string.exception_no_connection_try_again),
                R.drawable.ic_no_internet,
                listener,
                activity.getString(R.string.txt_close));
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
        DialogManager.showRetryDialog(activity,
                retryMessage,
                retryListener);
    }

    public static void showConfirmDialog(Activity activity,
                                         String pMessage,
                                         String pOKButton,
                                         String pCancelButton,
                                         ZPWOnEventConfirmDialogListener callback) {
        DialogManager.showConfirmDialog(activity,
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
            DialogManager.showVersionUpdateDialog(activity,
                    contentText,
                    newVersion,
                    activity.getString(R.string.btn_update),
                    null,
                    listener);
        } else {
            DialogManager.showVersionUpdateDialog(activity,
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
        DialogManager.showConfirmDialog(activity, message, btnConfirm, btnCancel, listener);
    }

    public static void showCustomDialog(Activity activity,
                                        int dialogType,
                                        String message,
                                        ZPWOnSweetDialogListener listener,
                                        String[] btnNames) {
        DialogManager.showMultiButtonDialog(activity,
                dialogType,
                -1,
                message,
                listener,
                btnNames);
    }

    private static boolean isShowingLoading() {
        return mProgressDialog != null && mProgressDialog.isShowing();
    }

    public static void hideLoading() {
        Timber.d("hideLoading");
        try {
            if (isShowingLoading()
                    && mProgressDialog.getOwnerActivity() != null
                    && !mProgressDialog.getOwnerActivity().isFinishing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
                Timber.d("hide loading success.");
            } else {
                Timber.d("hide loading fail because dialog null or not showing.");
            }
        } catch (Exception e) {
            Timber.w(e, "hideLoading throw exception [%s]", e.getMessage());
        }
    }

    public static void showLoading(final Activity activity) {
        Timber.d("showLoading activity[%s]", activity);
        try {
            if (activity == null) {
                return;
            }
            if (isShowingLoading()) {
                if (mProgressDialog.getOwnerActivity() == activity) {
                    Timber.d("Loading is showing.");
                    return;
                } else {
                    hideLoading();
                }
            }
            mProgressDialog = new SweetAlertDialog(activity,
                    SweetAlertDialog.PROGRESS_TYPE, R.style.alert_dialog_transparent);
            mProgressDialog.setOwnerActivity(activity);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        if (!activity.isFinishing()) {
                            hideLoading();
                            activity.onBackPressed();
                        }
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            if (!activity.isFinishing()) {
                Timber.d("show loading success.");
                mProgressDialog.show();
            } else {
                Timber.d("show loading fail because activity finishing.");
            }
        } catch (Exception e) {
            Timber.w(e, "showLoading throw exception [%s]", e.getMessage());
        }
    }


    public static SweetAlertDialog yesNoDialog(Activity pActivity, String pMessage, String pOKButton, String pCancelButton, final ZPWOnEventConfirmDialogListener callback) {
        SweetAlertDialog mDialog = new SweetAlertDialog(pActivity);
        mDialog.setContentHtmlText(pMessage);
        mDialog.setCancelText(pCancelButton);
        mDialog.setConfirmText(pOKButton);
        mDialog.setTitleText(pActivity.getString(vn.com.zalopay.wallet.R.string.dialog_title_confirm));
        mDialog.showCancelButton(true);
        mDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            public void onClick(SweetAlertDialog sDialog) {
                if (sDialog != null) {
                    sDialog.dismiss();
                }

                if (callback != null) {
                    callback.onCancelEvent();
                }

            }
        }).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            public void onClick(SweetAlertDialog sDialog) {
                if (sDialog != null) {
                    sDialog.dismiss();
                }

                if (callback != null) {
                    callback.onOKEvent();
                }

            }
        });
        return mDialog;
    }

    public static void closeAllDialog() {
        DialogManager.closeAllDialog();
    }
}

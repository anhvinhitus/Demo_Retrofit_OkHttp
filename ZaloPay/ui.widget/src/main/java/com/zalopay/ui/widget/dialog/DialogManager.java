package com.zalopay.ui.widget.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.zalopay.ui.widget.R;
import com.zalopay.ui.widget.dialog.listener.ZPWOnDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventUpdateListener;
import com.zalopay.ui.widget.dialog.listener.OnProgressDialogTimeoutListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnSweetDialogListener;

import java.lang.ref.WeakReference;

import timber.log.Timber;


/***
 * dialog wrapper class
 */
public class DialogManager {
    public static final int NORMAL_TYPE = 0;
    private static final int ERROR_TYPE = 1;
    private static final int SUCCESS_TYPE = 2;
    private static final int WARNING_TYPE = 3;
    private static final int UPDATE_TYPE = 4;
    private static final int PROGRESS_DIALOG_TIMEOUT = 35000;//ms

    static SweetAlertDialog mProgressDialog = null;
    static SweetAlertDialog mRetrySweetDialog = null;
    static SweetAlertDialog mConfirmDialog = null;
    static SweetAlertDialog mSweetDialogNoInternet = null;
    static SweetAlertDialog mAlertDialog = null;
    static SweetAlertDialog mVersionUpdateDialog = null;
    static SweetAlertDialog mMultiButtonDialog = null;
    static SweetAlertDialog mDialog = null;
    static long mLastShowProcessDialog = 0;

    public synchronized static void dismiss() {
        closeProcessDialog();
    }

    public synchronized static void closeAllDialog() {
        closeProcessDialog();

        if (mRetrySweetDialog != null && mRetrySweetDialog.isShowing()) {
            mRetrySweetDialog.dismiss();
        }
        if (mConfirmDialog != null && mConfirmDialog.isShowing()) {
            mConfirmDialog.dismiss();
        }
        if (mSweetDialogNoInternet != null && mSweetDialogNoInternet.isShowing()) {
            mSweetDialogNoInternet.dismiss();
        }
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        if (mMultiButtonDialog != null && mMultiButtonDialog.isShowing()) {
            mMultiButtonDialog.dismiss();
        }
    }

    private synchronized static void showProcessDialog(Activity pActivity, final long pStartTime, final OnProgressDialogTimeoutListener pCallback, long pTimeoutLoading) {
        try {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                Timber.d("There is a showing process dialog");
                return;
            }
            if (pActivity == null || pActivity.isFinishing()) {
                Timber.d("activity is null or finish - skip show loading dialog");
                return;
            }
            if (mProgressDialog == null) {
                mProgressDialog = new SweetAlertDialog(pActivity, SweetAlertDialog.PROGRESS_TYPE, R.style.alert_dialog_transparent);
            }
            final WeakReference<Activity> weakActivity = new WeakReference<>(pActivity);
            //delegate user back press to activity
            mProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        if (weakActivity.get() != null && !weakActivity.get().isFinishing()) {
                            weakActivity.get().onBackPressed();
                        }
                        return true;
                    }
                    return false;
                }
            });
            //set timeout for show progress dialog.
            mLastShowProcessDialog = pStartTime;
            final WeakReference<OnProgressDialogTimeoutListener> timeoutLoading = new WeakReference<>(pCallback);
            if (pCallback != null) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mProgressDialog != null && mProgressDialog.isShowing() && mLastShowProcessDialog == pStartTime) {
                            closeProcessDialog();
                            if (timeoutLoading.get() != null) {
                                timeoutLoading.get().onProgressTimeout();
                            }
                        }
                    }
                }, pTimeoutLoading);
            }
            mProgressDialog.show();
            Timber.d("start show loading dialog");
        } catch (Exception e) {
            Timber.w(e, "Exception show loading dialog");
        }
    }

    public synchronized static void showProcessDialog(Activity pActivity, OnProgressDialogTimeoutListener pCallback, long pTimeoutLoading) {
        showProcessDialog(pActivity, System.currentTimeMillis(), pCallback, pTimeoutLoading);
    }

    public synchronized static void showProcessDialog(Activity pActivity, OnProgressDialogTimeoutListener pCallback) {
        showProcessDialog(pActivity, System.currentTimeMillis(), pCallback, PROGRESS_DIALOG_TIMEOUT);
    }

    public synchronized static boolean isShowingProgressDialog() {
        return DialogManager.mProgressDialog != null && DialogManager.mProgressDialog.isShowing();
    }

    public synchronized static void closeProcessDialog() {
        try {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
                mLastShowProcessDialog = 0;
                Timber.d("close dialog");
            }
        } catch (Exception e) {
            Timber.w(e, "Exception close loading dialog");
        }
    }

    /****
     * show custom dialog(info, error, success, warning ...)
     */
    public synchronized static void showSweetDialogCustom(final Activity pActivity, final String pMessage, String pButtonText,
                                                          String pTitle, int pDialogType, final ZPWOnEventDialogListener callback) {
        try {
            if (mAlertDialog != null && mAlertDialog.isShowing()) {
                Timber.d("There are a sweet dialog is showing");
                return;
            }
            if (pActivity == null || pActivity.isFinishing()) {
                Timber.d("activity is null or finish");
                return;
            }
            if (mAlertDialog == null) {
                mAlertDialog = new SweetAlertDialog(pActivity, pDialogType, R.style.alert_dialog);
            }
            if (TextUtils.isEmpty(pTitle)) {
                switch (pDialogType) {
                    case NORMAL_TYPE:
                        pTitle = pActivity.getString(R.string.dialog_title_normal);
                        break;
                    case ERROR_TYPE:
                        pTitle = pActivity.getString(R.string.dialog_title_error);
                        break;
                    case UPDATE_TYPE:
                        pTitle = pActivity.getString(R.string.dialog_title_update);
                        break;
                    case SUCCESS_TYPE:
                        pTitle = pActivity.getString(R.string.dialog_title_success);
                        break;
                    case WARNING_TYPE:
                        pTitle = pActivity.getString(R.string.dialog_title_warning);
                        break;
                }
            }
            mAlertDialog.setConfirmText(pButtonText)
                    .setContentText(pMessage)
                    .setTitleText(pTitle);
            mAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    if (sweetAlertDialog != null) {
                        sweetAlertDialog.dismiss();
                        mAlertDialog = null;
                    }
                    if (callback != null) {
                        callback.onOKEvent();
                    }
                }
            });
            mAlertDialog.show();
        } catch (Exception e) {
            Timber.w(e, "Exception show custom alert dialog");
        }
    }

    /**
     * overload custom dialog (info, error, success, warning ...)
     *
     * @param pActivity   curent activity
     * @param pMessage    message from server
     * @param pButtonText text button
     * @param pDialogType dialog type
     * @param callback    call back
     */
    public synchronized static void showSweetDialogCustom(final Activity pActivity, final String pMessage, String pButtonText,
                                                          int pDialogType, final ZPWOnEventDialogListener callback) {
        try {
            if (mAlertDialog != null && mAlertDialog.isShowing()) {
                mAlertDialog.dismiss();
                Timber.d("There're a custom dialog showing - dismiss");
            }
            if (pActivity == null || pActivity.isFinishing()) {
                Timber.d("activity is null or finish");
                return;
            }
            if (mAlertDialog == null) {
                mAlertDialog = new SweetAlertDialog(pActivity, pDialogType, R.style.alert_dialog);
            }
            String pTitle = null;
            switch (pDialogType) {
                case NORMAL_TYPE:
                    pTitle = pActivity.getString(R.string.dialog_title_normal);
                    break;
                case ERROR_TYPE:
                    pTitle = pActivity.getString(R.string.dialog_title_error);
                    break;
                case UPDATE_TYPE:
                    pTitle = pActivity.getString(R.string.dialog_title_update);
                    break;
                case SUCCESS_TYPE:
                    pTitle = pActivity.getString(R.string.dialog_title_success);
                    break;
                case WARNING_TYPE:
                    pTitle = pActivity.getString(R.string.dialog_title_warning);
                    break;
            }
            mAlertDialog.setConfirmText(pButtonText)
                    .setContentHtmlText(pMessage)
                    .setTitleText(pTitle);
            mAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    if (sweetAlertDialog != null) {
                        sweetAlertDialog.dismiss();
                        mAlertDialog = null;
                    }
                    if (callback != null) {
                        callback.onOKEvent();
                    }
                }
            });
            mAlertDialog.show();

        } catch (Exception e) {
            Timber.w(e, "Exception show custom alert dialog");
        }
    }

    public synchronized static void showConfirmDialog(final Activity pActivity, String pTitle, final String pMessage, final String pOKButton,
                                                      final String pCancelButton, final ZPWOnEventConfirmDialogListener callback) {
        try {
            if (pActivity == null || pActivity.isFinishing()) {
                Timber.d("activity is null or finish");
                return;
            }
            if (mConfirmDialog != null && mConfirmDialog.isShowing()) {
                mConfirmDialog.dismiss();
                Timber.d("There're a confirm dialog showing - dismiss");
            }
            if (mConfirmDialog == null) {
                mConfirmDialog = new SweetAlertDialog(pActivity);
            }
            String title = pTitle;
            if (TextUtils.isEmpty(title)) {
                title = pActivity.getString(R.string.dialog_title_confirm);
            }
            mConfirmDialog.setContentHtmlText(pMessage)
                    .setCancelText(pCancelButton)
                    .setConfirmText(pOKButton)
                    .setTitleText(title)
                    .showCancelButton(true)
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            if (sDialog != null) {
                                sDialog.dismiss();
                                mConfirmDialog = null;
                            }
                            if (callback != null) {
                                callback.onCancelEvent();

                            }
                        }
                    }).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    if (sDialog != null) {
                        sDialog.dismiss();
                        mConfirmDialog = null;
                    }
                    if (callback != null) {
                        callback.onOKEvent();

                    }
                }
            });
            mConfirmDialog.show();
        } catch (Exception e) {
            Timber.w(e, "Exception show custom confirm dialog");
        }
    }

    public synchronized static void showVersionUpdateDialog(final Activity pActivity, final String pMessage, final String pVersion,
                                                            final String pOKButton, final String pCancelButton, final ZPWOnEventUpdateListener callback) {
        try {
            if (pActivity == null || pActivity.isFinishing()) {
                Timber.d("activity is null or finish");
                return;
            }
            if (mVersionUpdateDialog != null && mVersionUpdateDialog.isShowing()) {
                mVersionUpdateDialog.dismiss();
                Timber.d("There're a version update dialog showing - dismiss");
            }
            if (mVersionUpdateDialog == null) {
                boolean hasCancelButton = !TextUtils.isEmpty(pCancelButton);
                if (hasCancelButton) {
                    mVersionUpdateDialog = new SweetAlertDialog(pActivity, SweetAlertDialog.UPDATE, R.style.alert_dialog);
                    mVersionUpdateDialog.setCancelText(pCancelButton)
                            .showConfirmButton(true);
                } else {
                    mVersionUpdateDialog = new SweetAlertDialog(pActivity, SweetAlertDialog.UPDATE_TYPE, R.style.alert_dialog);
                }
                mVersionUpdateDialog.setUpdatetext(pOKButton)
                        .showCancelButton(hasCancelButton);
            }
            mVersionUpdateDialog.setContentHtmlText(pMessage)
                    .setVersionText(pVersion)
                    .setTitleText(pActivity.getString(R.string.dialog_title_update))
                    .setUpdateClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            if (sDialog != null) {
                                sDialog.dismiss();
                                mVersionUpdateDialog = null;
                            }
                            if (callback != null) {
                                callback.onUpdateListenner();
                            }
                        }
                    }).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    if (sDialog != null) {
                        sDialog.dismiss();
                        mVersionUpdateDialog = null;
                    }
                    if (callback != null) {
                        callback.onCancelListenner();
                    }
                }
            });
            mVersionUpdateDialog.show();
        } catch (Exception e) {
            Timber.w(e, "Exception show version update dialog");
        }
    }

    public synchronized static void showRetryDialog(final Activity pActivity, final String pMessage, final ZPWOnEventConfirmDialogListener callback) {
        try {
            if (pActivity == null || pActivity.isFinishing()) {
                Timber.d("activity is null or finish");
                return;
            }
            if (mRetrySweetDialog != null && mRetrySweetDialog.isShowing()) {
                mRetrySweetDialog.dismiss();
                Timber.d("There're a retry dialog showing - dismiss");
            }
            if (mRetrySweetDialog == null) {
                mRetrySweetDialog = new SweetAlertDialog(pActivity);
            }
            mRetrySweetDialog.setContentText(pMessage)
                    .setCancelText(pActivity.getString(R.string.dialog_cancel_button))
                    .setConfirmText(pActivity.getString(R.string.dialog_retry_button))
                    .showCancelButton(true)
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            if (callback != null) {
                                callback.onCancelEvent();
                            }
                            if (sDialog != null) {
                                sDialog.dismiss();
                                mRetrySweetDialog = null;
                            }

                        }
                    }).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    if (callback != null) {
                        callback.onOKEvent();

                    }
                    if (sDialog != null) {
                        sDialog.dismiss();
                        mRetrySweetDialog = null;
                    }
                }
            });
            closeProcessDialog();
            mRetrySweetDialog.show();
        } catch (Exception e) {
            Timber.w(e, "Exception show retry dialog");
        }
    }

    public synchronized static void showDrawableDialog(final Activity pActivity, String pTitle, String pContent, int pDrawable,
                                                       final ZPWOnSweetDialogListener pListener, String... pArrButton) {
        showMultiButtonDialog(pActivity, SweetAlertDialog.INFO_NO_ICON, pDrawable, pTitle, pContent, pListener, pArrButton);
    }

    public synchronized static void showMultiButtonDialog(final Activity pActivity, int pDialogType, int pIcoDrawable, String pTitle, String pContent,
                                                          final ZPWOnSweetDialogListener pListener, String... pArrButton) {
        try {
            if (pActivity == null || pActivity.isFinishing()) {
                Timber.d("activity is null or finish");
                return;
            }
            if (mMultiButtonDialog != null && mMultiButtonDialog.isShowing()) {
                mMultiButtonDialog.dismiss();
                Timber.d("There're a multi dialog showing - dismiss");
            }
            if (mMultiButtonDialog == null) {
                mMultiButtonDialog = new SweetAlertDialog(pActivity, pDialogType, R.style.alert_dialog);
            }
            if (pArrButton != null) {
                mMultiButtonDialog.setArrButton(pArrButton);
            }
            if (pIcoDrawable != -1) {
                mMultiButtonDialog.setCustomImage(pIcoDrawable);
            }
            mMultiButtonDialog.setTitleText(pTitle)
                    .setContentText(pContent)
                    .setCustomClickListener(new ZPWOnDialogListener() {
                        @Override
                        public void onCloseDialog(SweetAlertDialog sweetAlertDialog, int pIndexClick) {
                            if (pListener != null) {
                                pListener.onClickDiaLog(pIndexClick);
                            }
                            if (sweetAlertDialog != null) {
                                sweetAlertDialog.dismiss();
                                mMultiButtonDialog = null;
                            }
                        }
                    }).show();
        } catch (Exception e) {
            Timber.w(e, "Exception show multi button dialog");
        }
    }
}

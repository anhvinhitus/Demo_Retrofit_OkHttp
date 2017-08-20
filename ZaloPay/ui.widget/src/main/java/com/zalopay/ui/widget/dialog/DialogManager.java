package com.zalopay.ui.widget.dialog;

import android.app.Activity;
import android.os.Handler;
import android.text.TextUtils;

import com.zalopay.ui.widget.R;
import com.zalopay.ui.widget.dialog.listener.OnLoadingDialogTimeoutListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventUpdateListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnSweetDialogListener;

import java.lang.ref.WeakReference;

import timber.log.Timber;


/***
 * dialog wrapper class
 */
public class DialogManager {
    public static final int NORMAL_TYPE = 0;
    private static final int UPDATE_TYPE = 4;
    private static final int PROGRESS_DIALOG_TIMEOUT = 35000;//ms

    static SweetAlertDialog mLoadingDialog = null;
    static SweetAlertDialog mDialog = null;
    static long mLastShowProcessDialog = 0;

    public synchronized static void dismiss() {
        closeLoadDialog();
    }

    public synchronized static void closeAllDialog() {
        closeLoadDialog();
        closeShowDialog();
    }

    private synchronized static void showLoadingDialog(Activity pActivity, final long pStartTime,
                                                       final OnLoadingDialogTimeoutListener pCallback, long pTimeoutLoading) {
        try {
            if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
                Timber.d("There is a showing process dialog");
                return;
            }
            if (pActivity == null || pActivity.isFinishing()) {
                Timber.d("activity is null or finish - skip show loading dialog");
                return;
            }
            if (mLoadingDialog == null) {
                mLoadingDialog = new SweetAlertDialog(pActivity, SweetAlertDialog.PROGRESS_TYPE, R.style.alert_dialog_transparent);
            }
            //set timeout for show progress dialog.
            mLastShowProcessDialog = pStartTime;
            if (pCallback != null && pTimeoutLoading > 0) {
                final WeakReference<OnLoadingDialogTimeoutListener> timeoutLoading = new WeakReference<>(pCallback);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mLoadingDialog != null && mLoadingDialog.isShowing() && mLastShowProcessDialog == pStartTime) {
                            closeLoadDialog();
                            if (timeoutLoading.get() != null) {
                                timeoutLoading.get().onProgressTimeout();
                                Timber.d("callback on Timeout loading Dialog");
                            }
                            Timber.d("onTimeout loading Dialog");
                        }
                    }
                }, pTimeoutLoading);
            }
            closeShowDialog();
            mLoadingDialog.show();
            Timber.d("start show loading dialog");
        } catch (Exception e) {
            Timber.w(e, "Exception show loading dialog");
        }
    }

    public synchronized static void showLoadingDialog(Activity pActivity,
                                                      OnLoadingDialogTimeoutListener pCallback, long pTimeoutLoading) {
        showLoadingDialog(pActivity, System.currentTimeMillis(), pCallback, pTimeoutLoading);
    }

    public synchronized static void showLoadingDialog(Activity pActivity, OnLoadingDialogTimeoutListener pCallback) {
        showLoadingDialog(pActivity, System.currentTimeMillis(), pCallback, PROGRESS_DIALOG_TIMEOUT);
    }

    public synchronized static boolean showingLoadDialog() {
        return DialogManager.mLoadingDialog != null && DialogManager.mLoadingDialog.isShowing();
    }

    public synchronized static void closeLoadDialog() {
        try {
            if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
                mLoadingDialog = null;
                mLastShowProcessDialog = 0;
                Timber.d("close loading dialog");
            }
        } catch (Exception e) {
            Timber.d(e, "Exception close loading dialog");
        }
    }

    private static void closeShowDialog() {
        try {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
                mDialog = null;
            }
        } catch (Exception e) {
            Timber.d(e, "Exception close dialog");
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
    public synchronized static void showSweetDialogCustom(final Activity pActivity, final String pMessage, String pButtonText, int pDialogType, final ZPWOnEventDialogListener callback) {
        try {
            if (pActivity == null || pActivity.isFinishing()) {
                Timber.d("activity is null or finish");
                return;
            }
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
                mDialog = null;
                Timber.d("There're a custom dialog showing - dismiss");
            }
            mDialog = new SweetAlertDialog(pActivity, pDialogType, R.style.alert_dialog);
            String pTitle = null;
            if (pDialogType == UPDATE_TYPE) {
                pTitle = pActivity.getString(R.string.dialog_title_update);
            }

            mDialog.setConfirmText(pButtonText)
                    .setContentHtmlText(pMessage)
                    .setTitleText(pTitle);
            mDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    if (sweetAlertDialog != null) {
                        sweetAlertDialog.dismiss();
                        mDialog = null;
                    }
                    if (callback != null) {
                        callback.onOKEvent();
                    }
                }
            });
            mDialog.show();

        } catch (Exception e) {
            Timber.w(e, "Exception show custom alert dialog");
        }
    }

    public synchronized static void showConfirmDialog(final Activity pActivity, final String pMessage, final String pOKButton,
                                                      final String pCancelButton, final ZPWOnEventConfirmDialogListener callback) {
        try {
            if (pActivity == null || pActivity.isFinishing()) {
                Timber.d("activity is null or finish");
                return;
            }
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
                mDialog = null;
                Timber.d("There're a confirm dialog showing - dismiss");
            }
            mDialog = new SweetAlertDialog(pActivity);
            mDialog.setContentHtmlText(pMessage)
                    .setCancelText(pCancelButton)
                    .setConfirmText(pOKButton)
                    .showCancelButton(true)
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            if (sDialog != null) {
                                sDialog.dismiss();
                                mDialog = null;
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
                        mDialog = null;
                    }
                    if (callback != null) {
                        callback.onOKEvent();

                    }
                }
            });
            mDialog.show();
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
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
                mDialog = null;
                Timber.d("There're a version update dialog showing - dismiss");
            }
            boolean hasCancelButton = !TextUtils.isEmpty(pCancelButton);
            if (hasCancelButton) {
                mDialog = new SweetAlertDialog(pActivity, SweetAlertDialog.UPDATE, R.style.alert_dialog);
                mDialog.setCancelText(pCancelButton)
                        .showConfirmButton(true);
            } else {
                mDialog = new SweetAlertDialog(pActivity, SweetAlertDialog.UPDATE_TYPE, R.style.alert_dialog);
            }
            mDialog.setUpdatetext(pOKButton)
                    .showCancelButton(hasCancelButton);
            mDialog.setContentHtmlText(pMessage)
                    .setVersionText(pVersion)
                    .setTitleText(pActivity.getString(R.string.dialog_title_update))
                    .setUpdateClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            if (sDialog != null) {
                                sDialog.dismiss();
                                mDialog = null;
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
                        mDialog = null;
                    }
                    if (callback != null) {
                        callback.onCancelListenner();
                    }
                }
            });
            mDialog.show();
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
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
                mDialog = null;
                Timber.d("There're a retry dialog showing - dismiss");
            }
            mDialog = new SweetAlertDialog(pActivity);
            mDialog.setContentText(pMessage)
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
                                mDialog = null;
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
                        mDialog = null;
                    }
                }
            });
            closeLoadDialog();
            mDialog.show();
        } catch (Exception e) {
            Timber.w(e, "Exception show retry dialog");
        }
    }

    public synchronized static void showDrawableDialog(final Activity pActivity, String pContent, int pDrawable,
                                                       final ZPWOnSweetDialogListener pListener, String... pArrButton) {
        showMultiButtonDialog(pActivity, SweetAlertDialog.INFO_NO_ICON, pDrawable, pContent, pListener, pArrButton);
    }

    public synchronized static void showMultiButtonDialog(final Activity pActivity, int pDialogType, int pIcoDrawable, String pContent,
                                                          final ZPWOnSweetDialogListener pListener, String... pArrButton) {
        try {
            if (pActivity == null || pActivity.isFinishing()) {
                Timber.d("activity is null or finish");
                return;
            }
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
                mDialog = null;
                Timber.d("There're a multi dialog showing - dismiss");
            }
            mDialog = new SweetAlertDialog(pActivity, pDialogType, R.style.alert_dialog);
            if (pArrButton != null) {
                mDialog.setArrButton(pArrButton);
            }
            if (pIcoDrawable != -1) {
                mDialog.setCustomImage(pIcoDrawable);
            }
            mDialog.setContentText(pContent)
                    .setCustomClickListener(new ZPWOnDialogListener() {
                        @Override
                        public void onCloseDialog(SweetAlertDialog sweetAlertDialog, int pIndexClick) {
                            if (pListener != null) {
                                pListener.onClickDiaLog(pIndexClick);
                            }
                            if (sweetAlertDialog != null) {
                                sweetAlertDialog.dismiss();
                                mDialog = null;
                            }
                        }
                    });
            mDialog.show();
        } catch (Exception e) {
            Timber.w(e, "Exception show multi button dialog");
        }
    }
}

package com.zalopay.ui.widget.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import com.zalopay.ui.widget.R;
import com.zalopay.ui.widget.dialog.listener.ZPWOnDialogCustomEventListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventUpdateListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnProgressDialogTimeoutListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnSweetDialogListener;

import java.lang.ref.WeakReference;



/***
 * dialog wrapper class
 */
public class DialogManager {
    public static final int NORMAL_TYPE = 0;

    private static final int ERROR_TYPE = 1;

    private static final int SUCCESS_TYPE = 2;

    private static final int WARNING_TYPE = 3;

    private static final int CUSTOM_IMAGE_TYPE = 4;

    private static final int PROGRESS_TYPE = 5;

    private static final int UPDATE_TYPE = 6;
    private static final int PROGRESS_DIALOG_TIMEOUT = 35000;//ms
    private static SweetAlertDialog mProgressDialog = null;

    private static SweetAlertDialog mRetrySweetDialog = null;

    private static SweetAlertDialog mConfirmSweetDialog = null;

    private static SweetAlertDialog mCustomViewSweetDialog = null;
    private static SweetAlertDialog mSweetDialogNoInternet = null;
    private static SweetAlertDialog mAlertDialog = null;
    private static SweetAlertDialog mUpdateweetDialog = null;
    private static SweetAlertDialog mNewDiaLog = null;
    private static SweetAlertDialog mDialog = null;
    private static long mLastShowProcessDialog = 0;

    public synchronized static void dismiss() {
        closeProcessDialog();
    }

    public synchronized static void closeAllDialog() {
        closeProcessDialog();

        if (mRetrySweetDialog != null && mRetrySweetDialog.isShowing()) {
            mRetrySweetDialog.dismiss();
        }
        if (mConfirmSweetDialog != null && mConfirmSweetDialog.isShowing()) {
            mConfirmSweetDialog.dismiss();
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
        if (mNewDiaLog != null && mNewDiaLog.isShowing()) {
            mNewDiaLog.dismiss();
        }

    }

    /**
     * @param pActivity
     * @param pStartTime
     * @param pCallback
     */
    private synchronized static void showProcessDialog(Activity pActivity, final long pStartTime, final ZPWOnProgressDialogTimeoutListener pCallback, long pTimeoutLoading) {
        try {

            if (pActivity == null) {
                Log.d("showProcessDialog", "activity null !");
                return;
            }
            ;

            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                Log.d("showProcessDialog", "There is a showing process dialog!");
                return;
            }

            if (mProgressDialog == null) {
                mProgressDialog = new SweetAlertDialog(pActivity, SweetAlertDialog.PROGRESS_TYPE, R.style.alert_dialog_transparent);
            }


            if (mProgressDialog != null && pActivity != null && !pActivity.isFinishing()) {
                final Activity finalPActivity = pActivity;
                //capture when user click back.
                mProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            if (finalPActivity != null && !finalPActivity.isFinishing())
                                finalPActivity.onBackPressed();
                            return true;
                        }
                        return false;
                    }
                });

                //set timeout for show progress dialog.
                mLastShowProcessDialog = pStartTime;

                final WeakReference<ZPWOnProgressDialogTimeoutListener> timeoutLoading = new WeakReference<ZPWOnProgressDialogTimeoutListener>(pCallback);

                if (timeoutLoading.get() != null)
                    (new Handler()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (new Object()) {
                                try {
                                    if (mProgressDialog != null && mProgressDialog.isShowing() && mLastShowProcessDialog == pStartTime) {
                                        closeProcessDialog();

                                        if (finalPActivity != null && timeoutLoading.get() != null) {
                                            timeoutLoading.get().onProgressTimeout();
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e("showProcessDialog", "showProcessDialog"+ e);
                                }
                            }
                        }
                    }, pTimeoutLoading);

                mProgressDialog.show();

                Log.d("showProcessDialog", "Started a processing dialog");
            }
        } catch (Exception e) {
            Log.e("showProcessDialog","showProcessDialog"+ e);
        }
    }

    /**
     * show process dialog
     *
     * @param pActivity current activity
     * @param pCallback call back
     */
    public synchronized static void showProcessDialog(Activity pActivity, ZPWOnProgressDialogTimeoutListener pCallback, long pTimeoutLoading) {
        showProcessDialog(pActivity, System.currentTimeMillis(), pCallback, pTimeoutLoading);
    }

    /***
     * show process dialog
     *
     * @param pActivity
     * @param pCallback
     */
    public synchronized static void showProcessDialog(Activity pActivity, ZPWOnProgressDialogTimeoutListener pCallback) {
        showProcessDialog(pActivity, System.currentTimeMillis(), pCallback, PROGRESS_DIALOG_TIMEOUT);
    }

    public synchronized static boolean isShowingProgressDialog() {
        if (DialogManager.mProgressDialog != null && DialogManager.mProgressDialog.isShowing())
            return true;

        return false;
    }

    public synchronized static void closeProcessDialog() {
        try {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
                mLastShowProcessDialog = 0;

                Log.d("showProcessDialog", "Dismissed a processing dialog");

            }
            Log.d("showProcessDialog", " Call Dismissed but==" + mProgressDialog);
        } catch (Exception e) {
            Log.e("showProcessDialog", String.valueOf(e));
        }
    }

    /****
     * show custom dialog(info, error, success, warning ...)
     *
     * @param pActivity
     * @param pMessage
     * @param pButtonText
     * @param pTitle
     * @param pDialogType
     * @param callback
     */
    public synchronized static void showSweetDialogCustom(Activity pActivity, final String pMessage, String pButtonText, String pTitle, int pDialogType, final ZPWOnEventDialogListener callback) {
        try {
            if (pActivity == null || pActivity.isFinishing()) {
                Log.d("showSweetDialogCustom", "pActivity == null || pActivity.isFinishing()");
                return;
            }

            if (mAlertDialog == null) {
                mAlertDialog = new SweetAlertDialog(pActivity, pDialogType, R.style.alert_dialog);
            }

            if (mAlertDialog.isShowing()) {
                Log.d("showSweetDialogCustom", "===there're a alert dialog is showing===");
                return;
            }

            mAlertDialog.setConfirmText(pButtonText);
            mAlertDialog.setContentText(pMessage);

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

            mAlertDialog.setTitleText(pTitle);
            mAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    if (sweetAlertDialog != null) {
                        sweetAlertDialog.dismiss();
                    }

                    if (mAlertDialog != null) {
                        mAlertDialog = null;
                    }

                    if (callback != null) {
                        callback.onOKevent();
                    }
                }
            });
            mAlertDialog.show();

        } catch (Exception e) {
            Log.d("showProcessDialog","showProcessDialog"+ e);
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
    public synchronized static void showSweetDialogCustom(Activity pActivity, final String pMessage, String pButtonText, int pDialogType, final ZPWOnEventDialogListener callback) {
        try {
            if (pActivity == null || pActivity.isFinishing()) {
                Log.d("showSweetDialogCustom", "pActivity == null || pActivity.isFinishing()");
                return;
            }

            if (mAlertDialog == null) {
                mAlertDialog = new SweetAlertDialog(pActivity, pDialogType, R.style.alert_dialog);
            }

            if (mAlertDialog.isShowing()) {
                Log.d("showSweetDialogCustom", "===there're a alert dialog is showing===");
                return;
            }

            mAlertDialog.setConfirmText(pButtonText);
            mAlertDialog.setContentHtmlText(pMessage);

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

            mAlertDialog.setTitleText(pTitle);

            mAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    if (sweetAlertDialog != null) {
                        sweetAlertDialog.dismiss();
                    }

                    if (mAlertDialog != null) {
                        mAlertDialog = null;
                    }

                    if (callback != null) {
                        callback.onOKevent();
                    }
                }
            });
            mAlertDialog.show();

        } catch (Exception e) {
            Log.d("showProcessDialog", "showProcessDialog"+ e);
        }
    }

    /**
     * Show dialog confirm
     *
     * @param pActivity     cuarrent activity
     * @param pMessage      message from server
     * @param pOKButton     text button confirm
     * @param pCancelButton text button cancel
     * @param callback      call back
     */
    public synchronized static void showSweetDialogConfirm(final Activity pActivity, final String pMessage, final String pOKButton, final String pCancelButton, final ZPWOnEventConfirmDialogListener callback) {

        try {
            if (pActivity == null || pActivity.isFinishing()) {
                Log.d("showSweetDialogConfirm", "pActivity == null || pActivity.isFinishing()");
                return;
            }

            if (mConfirmSweetDialog == null) {
                mConfirmSweetDialog = new SweetAlertDialog(pActivity);
            }

            if (mConfirmSweetDialog.isShowing()) {
                Log.d("showSweetDialogConfirm", "===there're a confirm dialog is showing===");
                return;
            }

            mConfirmSweetDialog.setContentHtmlText(pMessage);
            mConfirmSweetDialog.setCancelText(pCancelButton);
            mConfirmSweetDialog.setConfirmText(pOKButton);
            mConfirmSweetDialog.setTitleText(pActivity.getString(R.string.dialog_title_confirm));
            mConfirmSweetDialog.showCancelButton(true);
            mConfirmSweetDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    if (sDialog != null) {
                        sDialog.dismiss();
                    }

                    if (mConfirmSweetDialog != null) {
                        mConfirmSweetDialog = null;
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
                    }
                    if (mConfirmSweetDialog != null) {
                        mConfirmSweetDialog = null;
                    }

                    if (callback != null) {
                        callback.onOKevent();

                    }
                }
            }).show();
        } catch (Exception e) {
            Log.e("showProcessDialog", "showProcessDialog"+ e);
        }
    }

    /***
     * show dialog option with title "THÔNG BÁO"
     *
     * @param pActivity
     * @param pMessage
     * @param pOKButton
     * @param pCancelButton
     * @param callback
     */
    public synchronized static void showSweetDialogOptionNotice(final Activity pActivity, final String pMessage, final String pOKButton, final String pCancelButton, final ZPWOnEventConfirmDialogListener callback) {

        try {
            if (pActivity == null || pActivity.isFinishing()) {
                Log.d("showDialogOptionNotice", "pActivity == null || pActivity.isFinishing()");
                return;
            }

            if (mConfirmSweetDialog == null) {
                mConfirmSweetDialog = new SweetAlertDialog(pActivity);
            }

            if (mConfirmSweetDialog.isShowing()) {
                Log.d("showDialogOptionNotice", "===there're a mConfirmSweetDialog is showing====");
                return;
            }

            mConfirmSweetDialog.setContentHtmlText(pMessage);
            mConfirmSweetDialog.setCancelText(pCancelButton);
            mConfirmSweetDialog.setConfirmText(pOKButton);
            mConfirmSweetDialog.setTitleText(pActivity.getString(R.string.dialog_title_normal));
            mConfirmSweetDialog.showCancelButton(true);
            mConfirmSweetDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    if (sDialog != null) {
                        sDialog.dismiss();
                    }

                    if (mConfirmSweetDialog != null) {
                        mConfirmSweetDialog = null;
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
                    }

                    if (mConfirmSweetDialog != null) {
                        mConfirmSweetDialog = null;
                    }

                    if (callback != null) {
                        callback.onOKevent();

                    }
                }
            }).show();
        } catch (Exception e) {
            Log.e("showProcessDialog", "showProcessDialog"+ e);
        }
    }

    /**
     * show update version from app
     *
     * @param pActivity     current activity
     * @param pMessage      get from server
     * @param pVersion      new version
     * @param pOKButton     text button update
     * @param pCancelButton text cancel button
     * @param callback      call back
     */
    public synchronized static void showSweetDialogUpdate(final Activity pActivity, final String pMessage, final String pVersion, final String pOKButton, final String pCancelButton, final ZPWOnEventUpdateListener callback) {

        try {
            if (pActivity == null || pActivity.isFinishing())
                return;

            if (mUpdateweetDialog == null) {
                if (!TextUtils.isEmpty(pCancelButton)) {
                    mUpdateweetDialog = new SweetAlertDialog(pActivity, SweetAlertDialog.UPDATE, R.style.alert_dialog);
                    mUpdateweetDialog.showCancelButton(true);
                    mUpdateweetDialog.setCancelText(pCancelButton);
                    mUpdateweetDialog.showConfirmButton(true);
                    mUpdateweetDialog.setConfirmText(pOKButton);

                } else {
                    mUpdateweetDialog = new SweetAlertDialog(pActivity, SweetAlertDialog.UPDATE_TYPE, R.style.alert_dialog);
                    mUpdateweetDialog.showCancelButton(false);
                    mUpdateweetDialog.setUpdatetext(pOKButton);
                }
            }
            if (mUpdateweetDialog.isShowing())
                return;
            mUpdateweetDialog.setContentHtmlText(pMessage);
            mUpdateweetDialog.setVersionText(pVersion);
            mUpdateweetDialog.setTitleText(pActivity.getString(R.string.dialog_title_update));
            mUpdateweetDialog.setUpdateClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    if (sDialog != null) {
                        sDialog.dismiss();
                    }

                    if (mUpdateweetDialog != null) {
                        mUpdateweetDialog = null;
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
                    }
                    if (mUpdateweetDialog != null) {
                        mUpdateweetDialog = null;
                    }
                    if (callback != null) {
                        callback.onCancelListenner();

                    }
                }
            }).show();
        } catch (Exception e) {
            Log.e("showProcessDialog", "showProcessDialog"+ e);
        }
    }

    /***
     * show update version from app
     *
     * @param pActivity
     * @param pMessage
     * @param pVersion
     * @param pOKButton
     * @param callback
     */
    public synchronized static void showSweetDialogUpdate(final Activity pActivity, final String pMessage, final String pVersion, final String pOKButton, final ZPWOnEventUpdateListener callback) {

        try {
            if (pActivity == null || pActivity.isFinishing()) {
                Log.d("showSweetDialogUpdate", "pActivity == null || pActivity.isFinishing()");
                return;
            }

            if (mUpdateweetDialog == null) {
                mUpdateweetDialog = new SweetAlertDialog(pActivity, SweetAlertDialog.UPDATE_TYPE, R.style.alert_dialog);
                mUpdateweetDialog.showCancelButton(false);
                mUpdateweetDialog.setUpdatetext(pOKButton);
            }
            if (mUpdateweetDialog.isShowing()) {
                Log.d("showSweetDialogUpdate", "===there're a mUpdateweetDialog is showing====");
                return;
            }
            mUpdateweetDialog.setContentHtmlText(pMessage);
            mUpdateweetDialog.setVersionText(pVersion);
            mUpdateweetDialog.setTitleText(pActivity.getString(R.string.dialog_title_update));
            mUpdateweetDialog.setUpdateClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    if (sDialog != null) {
                        sDialog.dismiss();
                    }

                    if (mUpdateweetDialog != null) {
                        mUpdateweetDialog = null;
                    }

                    if (callback != null) {
                        callback.onUpdateListenner();

                    }
                }
            }).show();
        } catch (Exception e) {
            Log.e("showProcessDialog","showProcessDialog"+ e);
        }
    }

    /**
     * Dialog custom content view
     *
     * @param pActivity    Activity
     * @param pTitle       String title
     * @param pMessage     String mesage
     * @param pRightButton Text Right button or null
     * @param pLeftButton  Text Left button or null
     * @param callback     call back
     */
    public synchronized static void showSweetDialogNormal(final Activity pActivity, final String pTitle, final String pMessage, final String pRightButton, final String pLeftButton, final ZPWOnDialogCustomEventListener callback) {

        try {
            if (pActivity == null || pActivity.isFinishing()) {
                Log.d("showSweetDialogNormal", "pActivity == null || pActivity.isFinishing()");
                return;
            }

            if (mCustomViewSweetDialog == null)
                mCustomViewSweetDialog = new SweetAlertDialog(pActivity, SweetAlertDialog.CUSTOM_CONTENT_VIEW, R.style.alert_dialog);

            if (mCustomViewSweetDialog.isShowing()) {
                Log.d("showSweetDialogNormal", "===there're a mCustomViewSweetDialog is showing===");
                return;
            }

            mCustomViewSweetDialog.setCancelText(pLeftButton);
            mCustomViewSweetDialog.setConfirmText(pRightButton);
            mCustomViewSweetDialog.setTitleText(pTitle);
            mCustomViewSweetDialog.setContentHtmlText(pMessage);
            mCustomViewSweetDialog.showCancelButton(true);
            mCustomViewSweetDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    if (sDialog != null) {
                        sDialog.dismiss();
                    }

                    if (mCustomViewSweetDialog != null) {
                        mCustomViewSweetDialog = null;
                    }

                    if (callback != null) {
                        callback.onLeftButtonClick();

                    }
                }
            }).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    if (sDialog != null) {
                        sDialog.dismiss();
                    }
                    if (mCustomViewSweetDialog != null) {
                        mCustomViewSweetDialog = null;
                    }
                    if (callback != null) {
                        callback.onRightButtonClick();

                    }
                }
            }).show();
        } catch (Exception e) {
            Log.e("showProcessDialog", "showProcessDialog"+ e);
        }
    }

    public synchronized static void closeNetworkingDialog() {
        if (mSweetDialogNoInternet.isShowing()) {
            mSweetDialogNoInternet.dismiss();
        }
    }

    /**
     * dialog for turn on internet
     *
     * @param pActivity    Activity
     * @param pMessage     String mesage
     * @param pRightButton Text Right button or null
     * @param pLeftButton  Text Left button or null
     * @param callback     call back
     */
    public synchronized static void showSweetDialogNoInternet(final Activity pActivity, final String pMessage, final String pRightButton, final String pLeftButton, final ZPWOnDialogCustomEventListener callback) {

        try {
            if (pActivity == null || pActivity.isFinishing()) {
                return;
            }

            if (mSweetDialogNoInternet == null)
                mSweetDialogNoInternet = new SweetAlertDialog(pActivity, SweetAlertDialog.NO_INTERNET, R.style.alert_dialog);

            if (mSweetDialogNoInternet.isShowing()) {
                Log.d("showDialogNoInternet", "===there're a dialog is showing");
                return;
            }

            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                closeProcessDialog();
            }

            if (mAlertDialog != null && mAlertDialog.isShowing()) {
                mAlertDialog.dismiss();
            }

            if (mConfirmSweetDialog != null && mConfirmSweetDialog.isShowing()) {
                mConfirmSweetDialog.dismiss();
            }

            if (mRetrySweetDialog != null && mRetrySweetDialog.isShowing()) {
                mRetrySweetDialog.dismiss();
            }

            mSweetDialogNoInternet.setCancelText(pLeftButton);
            mSweetDialogNoInternet.setConfirmText(pRightButton);
            mSweetDialogNoInternet.setTitleText("");
            mSweetDialogNoInternet.setCustomViewContent(pMessage);
            mSweetDialogNoInternet.showCancelButton(true);
            mSweetDialogNoInternet.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    if (sDialog != null) {
                        sDialog.dismiss();
                    }

                    if (mSweetDialogNoInternet != null) {
                        mSweetDialogNoInternet = null;
                    }

                    if (callback != null) {
                        callback.onLeftButtonClick();

                    }
                }
            }).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    if (sDialog != null) {
                        sDialog.dismiss();
                    }
                    if (mSweetDialogNoInternet != null) {
                        mSweetDialogNoInternet = null;
                    }
                    if (callback != null) {
                        callback.onRightButtonClick();

                    }
                }
            }).show();
        } catch (Exception e) {
            Log.e("showProcessDialog", "showProcessDialog"+ e);
        }
    }

    /**
     * retry dialog
     *
     * @param pActivity current activity
     * @param pMessage  message from server
     * @param callback  call back
     */
    public synchronized static void showSweetDialogRetry(final Activity pActivity, final String pMessage, final ZPWOnEventConfirmDialogListener callback) {
        try {
            if (pActivity == null || pActivity.isFinishing()) {
                Log.d("showSweetDialogRetry", "pActivity == null || pActivity.isFinishing()");
                return;
            }

            if (mRetrySweetDialog != null && mRetrySweetDialog.isShowing()) {
                Log.d("showSweetDialogRetry", "===mRetrySweetDialog.isShowing()===");
                mRetrySweetDialog.dismiss();
            }

            if (mRetrySweetDialog == null) {
                mRetrySweetDialog = new SweetAlertDialog(pActivity);
            }

            closeProcessDialog();

            mRetrySweetDialog.setContentText(pMessage);
            mRetrySweetDialog.setCancelText(pActivity.getString(R.string.dialog_cancel_button));
            mRetrySweetDialog.setConfirmText(pActivity.getString(R.string.dialog_retry_button));
            mRetrySweetDialog.showCancelButton(true);
            mRetrySweetDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    if (callback != null) {
                        callback.onCancelEvent();
                    }

                    if (sDialog != null) {
                        sDialog.dismiss();
                    }

                    if (mRetrySweetDialog != null) {
                        mRetrySweetDialog = null;
                    }

                }
            }).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    if (callback != null) {
                        callback.onOKevent();

                    }
                    if (sDialog != null) {
                        sDialog.dismiss();
                    }
                    if (mRetrySweetDialog != null) {
                        mRetrySweetDialog = null;
                    }
                }
            }).show();
        } catch (Exception e) {
            Log.e("showSweetDialogRetry", "showProcessDialog"+ e);
        }
    }

    /***
     * @param pActivity
     * @param pType
     * @param pTitle
     * @param pContent
     * @param pArrButton
     * @param pListener
     */
    public synchronized static void showDialog(final Activity pActivity, int pType, String pTitle, String pContent, final ZPWOnSweetDialogListener pListener, String... pArrButton) {

        try {
            if (pActivity == null || pActivity.isFinishing()) {
                return;
            }

            if (mDialog == null) {
                if (pType < 5 && pType >= 0) {
                    mDialog = new SweetAlertDialog(pActivity, pType, R.style.alert_dialog);
                } else {
                    mDialog = new SweetAlertDialog(pActivity, SweetAlertDialog.NORMAL_TYPE, R.style.alert_dialog);
                }
            }

            if (mDialog.isShowing()) {
                return;
            }
            if (pArrButton != null) {
                mDialog.setArrButton(pArrButton);
            }

            mDialog.setTitleText(pTitle);
            mDialog.setContentText(pContent);
            mDialog.setCustomClickListener(new ZPWOnDialogListener() {
                @Override
                public void onCloseDialog(SweetAlertDialog sDialog, int pIndexClick) {
                    Log.d("Click Dialog", String.valueOf(pIndexClick));
                    if (pListener != null) {
                        pListener.onClickDiaLog(pIndexClick);
                    }

                    if (sDialog != null) {
                        sDialog.dismiss();
                    }

                    if (mDialog != null) {
                        mDialog = null;
                    }
                }
            }).show();
        } catch (Exception e) {
            Log.e("showDialogCustomView", "showProcessDialog"+ e);
        }
    }

    /***
     * @param pActivity
     * @param pTitle
     * @param pContent
     * @param pDrawable
     * @param pArrButton
     * @param pListener
     */
    public synchronized static void showDialog(final Activity pActivity, String pTitle, String pContent, int pDrawable, final ZPWOnSweetDialogListener pListener, String... pArrButton) {

        try {
            if (pActivity == null || pActivity.isFinishing())
                return;

            if (mDialog == null) {
                mDialog = new SweetAlertDialog(pActivity, SweetAlertDialog.INFO_NO_ICON, R.style.alert_dialog);
            }

            if (mDialog.isShowing()) {
                Log.d("showDialogCustomView", "===there a custom view dialog showing===");
                return;
            }
            if (pArrButton != null)
                mDialog.setArrButton(pArrButton);

            mDialog.setTitleText(pTitle);
            mDialog.setContentText(pContent);
            mDialog.setCustomImage(pDrawable);
            mDialog.setCustomClickListener(new ZPWOnDialogListener() {
                @Override
                public void onCloseDialog(SweetAlertDialog sDialog, int pIndexClick) {
                    Log.d("Click Dialog", String.valueOf(pIndexClick));
                    if (pListener != null) {
                        pListener.onClickDiaLog(pIndexClick);
                    }

                    if (sDialog != null) {
                        sDialog.dismiss();
                    }
                    if (mDialog != null) {
                        mDialog = null;
                    }
                }
            }).show();
        } catch (Exception e) {
            Log.e("showDialogCustomView", "showProcessDialog"+ e);
        }
    }

    /**
     * All Dialog
     *
     * @param pActivity
     * @param pType
     * @param pTitle
     * @param pContent
     * @param pListener
     * @param pArrButton
     */
    public synchronized static void showSweetDialog(final Activity pActivity, int pType, String pTitle, String pContent, final ZPWOnSweetDialogListener pListener, String... pArrButton) {

        try {
            if (pActivity == null || pActivity.isFinishing())
                return;

            if (mNewDiaLog == null) {
                mNewDiaLog = new SweetAlertDialog(pActivity, pType, R.style.alert_dialog);
            }

            if (mNewDiaLog.isShowing()) {
                Log.d("showDialogCustomView", "===there a custom view dialog showing===");
                return;
            }
            if (pType == SweetAlertDialog.NO_INTERNET && mProgressDialog != null && mProgressDialog.isShowing()) {
                closeProcessDialog();
            }
            // if apply all SweetAlertDialog delete here
            if (mAlertDialog != null && mAlertDialog.isShowing()) {
                mAlertDialog.dismiss();
            }

            if (mConfirmSweetDialog != null && mConfirmSweetDialog.isShowing()) {
                mConfirmSweetDialog.dismiss();
            }

            if (mRetrySweetDialog != null && mRetrySweetDialog.isShowing()) {
                mRetrySweetDialog.dismiss();
            }
            //
            if (pArrButton != null)
                mNewDiaLog.setArrButton(pArrButton);

            mNewDiaLog.setTitleText(pTitle);

            mNewDiaLog.setContentText(pContent);

            mNewDiaLog.setCustomClickListener(new ZPWOnDialogListener() {
                @Override
                public void onCloseDialog(SweetAlertDialog sweetAlertDialog, int pIndexClick) {
                    Log.d("Click Dialog", String.valueOf(pIndexClick));
                    if (pListener != null) {
                        pListener.onClickDiaLog(pIndexClick);
                    }
                    if (sweetAlertDialog != null) {
                        sweetAlertDialog.dismiss();
                    }
                    if (mNewDiaLog != null) {
                        mNewDiaLog = null;
                    }
                }
            }).show();
        } catch (Exception e) {
            Log.e("showDialogCustomView", "showProcessDialog"+ e);
        }
    }



    /**
     * Dialog custom Image
     *
     * @param pActivity
     * @param pTitle
     * @param pContent
     * @param pDrawable
     * @param pListener
     * @param pArrButton
     */
    public synchronized static void showSweetDialog(final Activity pActivity, String pTitle, String pContent, Drawable pDrawable, final ZPWOnSweetDialogListener pListener, String... pArrButton) {

        try {
            if (pActivity == null || pActivity.isFinishing())
                return;

            if (mNewDiaLog == null) {
                mNewDiaLog = new SweetAlertDialog(pActivity, SweetAlertDialog.INFO_NO_ICON, R.style.alert_dialog);
            }

            if (mNewDiaLog.isShowing()) {
                Log.d("showDialogCustomView", "===there a custom view dialog showing===");
                return;
            }
            if (pArrButton != null)
                mNewDiaLog.setArrButton(pArrButton);

            mNewDiaLog.setTitleText(pTitle);

            mNewDiaLog.setContentText(pContent);

            mNewDiaLog.setCustomImage(pDrawable);

            mNewDiaLog.setCustomClickListener(new ZPWOnDialogListener() {
                @Override
                public void onCloseDialog(SweetAlertDialog sweetAlertDialog, int pIndexClick) {
                    Log.d("Click Dialog", String.valueOf(pIndexClick));
                    if (pListener != null) {
                        pListener.onClickDiaLog(pIndexClick);
                    }
                    if (sweetAlertDialog != null) {
                        sweetAlertDialog.dismiss();
                    }
                    if (mNewDiaLog != null) {
                        mNewDiaLog = null;
                    }
                }
            }).show();
        } catch (Exception e) {
            Log.e("showDialogCustomView", "showProcessDialog"+ e);
        }
    }

    /**
     *
     * @param pActivity
     * @param pTitle
     * @param pContent
     * @param pVersion
     * @param pListener
     * @param pArrButton
     */

    public synchronized static void showSweetDialog(final Activity pActivity, String pTitle, String pContent, String pVersion, final ZPWOnSweetDialogListener pListener, String... pArrButton) {

        try {
            if (pActivity == null || pActivity.isFinishing())
                return;

            if (mNewDiaLog == null && pArrButton != null) {
                if (pArrButton.length == 1) {
                    mNewDiaLog = new SweetAlertDialog(pActivity, SweetAlertDialog.UPDATE_TYPE, R.style.alert_dialog);
                    mNewDiaLog.setUpdatetext(pArrButton[0]);
                } else {
                    mNewDiaLog = new SweetAlertDialog(pActivity, SweetAlertDialog.UPDATE, R.style.alert_dialog);
                    mNewDiaLog.setArrButton(pArrButton);
                }
            }
            if (mNewDiaLog.isShowing())
                return;

            mNewDiaLog.setContentText(pContent);
            mNewDiaLog.setVersionText(pVersion);
            mNewDiaLog.setTitleText(pTitle);
            mNewDiaLog.setCustomClickListener(new ZPWOnDialogListener() {
                @Override
                public void onCloseDialog(SweetAlertDialog sweetAlertDialog, int pIndexClick) {
                    Log.d("Click Dialog", String.valueOf(pIndexClick));
                    if (pListener != null) {
                        pListener.onClickDiaLog(pIndexClick);
                    }
                    if (sweetAlertDialog != null) {
                        sweetAlertDialog.dismiss();
                    }
                    if (mNewDiaLog != null) {
                        mNewDiaLog = null;
                    }
                }
            }).show();
        } catch (Exception e) {
            Log.e("showDialogCustomView", "showProcessDialog"+ e);
        }
    }
}

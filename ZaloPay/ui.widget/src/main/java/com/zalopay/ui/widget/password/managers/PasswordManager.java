package com.zalopay.ui.widget.password.managers;

import android.app.Activity;
import android.support.design.widget.BottomSheetBehavior;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.zalopay.ui.widget.UIBottomSheetDialog;
import com.zalopay.ui.widget.password.bottomsheet.PasswordViewRender;
import com.zalopay.ui.widget.password.interfaces.IBuilder;
import com.zalopay.ui.widget.password.interfaces.IControl;
import com.zalopay.ui.widget.password.interfaces.IPinCallBack;

import java.lang.ref.WeakReference;

/**
 * Created by lytm on 23/05/2017.
 */

public class PasswordManager {
    private static final String TAG = PasswordManager.class.getSimpleName();
    private WeakReference<IPinCallBack> mIPinCallBack;
    private WeakReference<Activity> mActivity;
    private IBuilder mIBuilder;
    private UIBottomSheetDialog mUiBottomSheetDialog;

    /**
     * @param pActivity
     * @param pTitle
     * @param pLogoPath
     * @param pIPinCallBack
     */
    public PasswordManager(Activity pActivity, String pTitle, String pLogoPath, boolean pFingerPrint, IPinCallBack pIPinCallBack) {
        mIPinCallBack = new WeakReference<>(pIPinCallBack);
        mActivity = new WeakReference<>(pActivity);
        View contentView = View.inflate(mActivity.get(), com.zalopay.ui.widget.R.layout.view_pin_code, null);
        mIBuilder = PasswordViewRender.getBuilder()
                .setView(contentView)
                .setIFPinCallBack(mIPinCallBack.get())
                .setIFControl(mControl)
                .setTitle(pTitle)
                .setLogoPath(pLogoPath)
                .setFingerPrint(pFingerPrint);
        mUiBottomSheetDialog = new UIBottomSheetDialog(mActivity.get(), com.zalopay.ui.widget.R.style.CoffeeDialog, mIBuilder.build());
        mUiBottomSheetDialog.setCanceledOnTouchOutside(false);
    }

    public void showPinView() {
        if (mUiBottomSheetDialog != null && !isShowing()) {
            mUiBottomSheetDialog.show();
            mUiBottomSheetDialog.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    public boolean isShowing() {
        return mUiBottomSheetDialog.isShowing();
    }

    public void closePinView() {
        if (mUiBottomSheetDialog != null && isShowing() && mIBuilder != null) {
            mIBuilder.showLoadding(false);
            mIBuilder.clearText();
            mUiBottomSheetDialog.dismiss();
        }
        mActivity = null;
        mIPinCallBack = null;
        mIBuilder = null;
        mUiBottomSheetDialog = null;
    }

    /**
     * @param pMessage
     */
    public void setErrorMessage(String pMessage) {
        if (!TextUtils.isEmpty(pMessage) && mIBuilder != null && mActivity.get() != null && !mActivity.get().isFinishing()) {
            mIBuilder.showLoadding(false);
            mIBuilder.setErrorMessage(mActivity.get(), pMessage);
        }
    }

    public void setContent(String pTitle, String pLogoPath) {
        if (mIBuilder == null) {
            Log.e("setContent", "mBuilder is null");
            return;
        }
        mIBuilder.setTitle(pTitle);
        mIBuilder.setLogoPath(pLogoPath);
    }

    public void showLoading(boolean pShowing) {
        if (mIBuilder == null) {
            Log.e(TAG, "mBuilder is null");
            return;
        }
        mIBuilder.showLoadding(pShowing);
    }


    public void showFingerPrintCheckBox(boolean pShowing) {
        if (mIBuilder == null) {
            Log.e(TAG, "mBuilder is null");
            return;
        }
        mIBuilder.setFingerPrint(pShowing);
    }

    public void LockView(boolean isLock) {
        if (mUiBottomSheetDialog == null) {
            Log.d(TAG, "mUiBottomSheetDialog is null");
            return;
        }
        mUiBottomSheetDialog.setDisableHidden(isLock);
        mIBuilder.setLockControl(isLock);
    }


    private IControl mControl = new IControl() {
        @Override
        public void clickCancel() {
            if (mIBuilder == null) {
                Log.e(TAG, "mBuilder is null");
                return;
            }
            if (mUiBottomSheetDialog != null && mUiBottomSheetDialog.isShowing()) {
                mIBuilder.showLoadding(false);
                mIBuilder.clearText();
                mIBuilder.getIFPinCallBack().onCancel();
                mUiBottomSheetDialog.setState(BottomSheetBehavior.STATE_HIDDEN);
            }

        }
    };
}

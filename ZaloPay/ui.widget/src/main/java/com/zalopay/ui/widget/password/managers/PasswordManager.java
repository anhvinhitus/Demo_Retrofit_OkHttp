package com.zalopay.ui.widget.password.managers;

import android.app.Activity;
import android.support.design.widget.BottomSheetBehavior;
import android.text.TextUtils;
import android.view.View;

import com.zalopay.ui.widget.UIBottomSheetDialog;
import com.zalopay.ui.widget.password.bottomsheet.PasswordViewRender;
import com.zalopay.ui.widget.password.interfaces.IBuilder;
import com.zalopay.ui.widget.password.interfaces.IControl;

import java.lang.ref.WeakReference;

import timber.log.Timber;

/**
 * Created by lytm on 23/05/2017.
 */

public class PasswordManager {
    private IBuilder mIBuilder;
    private UIBottomSheetDialog mUiBottomSheetDialog;
    private WeakReference<Activity> mActivity;
    private IControl mControl = new IControl() {
        @Override
        public void onClose() {
            if (mIBuilder != null && mIBuilder.getIFPinCallBack() != null) {
                mIBuilder.getIFPinCallBack().onClose();
            }
            close();
        }
    };

    public PasswordManager(Activity pActivity) {
        mActivity = new WeakReference<>(pActivity);
        View contentView = View.inflate(pActivity, com.zalopay.ui.widget.R.layout.view_pin_code, null);
        mIBuilder = PasswordViewRender.getBuilder();
        mIBuilder.setView(contentView)
                .setIFControl(mControl);
    }

    public synchronized void buildDialog() {
        if (mActivity.get() == null || mActivity.get().isFinishing()) {
            Timber.d("activity is destroy");
            return;
        }
        mUiBottomSheetDialog = new UIBottomSheetDialog(mActivity.get(), com.zalopay.ui.widget.R.style.CoffeeDialog, mIBuilder.build());
        mUiBottomSheetDialog.setCanceledOnTouchOutside(false);
    }

    public IBuilder getBuilder() {
        return mIBuilder;
    }


    public synchronized void show() {
        if (!isShowing()) {
            mUiBottomSheetDialog.show();
            mUiBottomSheetDialog.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    public synchronized boolean isShowing() {
        return mUiBottomSheetDialog != null && mUiBottomSheetDialog.isShowing();
    }

    public synchronized void close() {
        if (mIBuilder == null) {
            return;
        }

        if (mUiBottomSheetDialog != null && isShowing()) {
            mUiBottomSheetDialog.dismiss();
            Timber.d("dismiss password popup");
        }
        Timber.d("release password popup");
        mIBuilder = null;
        mUiBottomSheetDialog = null;
        mActivity = null;
    }

    public void setError(String pMessage) {
        if (!TextUtils.isEmpty(pMessage) && mIBuilder != null) {
            mIBuilder.showLoadding(false);
            mIBuilder.setError(pMessage);
        }
    }

    public void showLoading(boolean pShowing) {
        if (mIBuilder == null) {
            Timber.d("mIBuilder is null");
            return;
        }
        mIBuilder.showLoadding(pShowing);
    }

    public void setTitle(String pTitle) {
        if (mIBuilder == null) {
            Timber.d("mIBuilder is null");
            return;
        }
        mIBuilder.setTitle(pTitle);
    }

    public void lock() {
        disable(true);
    }

    public void unlock() {
        disable(false);
    }

    private void disable(boolean disable) {
        if (mUiBottomSheetDialog == null) {
            Timber.d("mUiBottomSheetDialog is null");
            return;
        }
        mUiBottomSheetDialog.preventDrag(disable);
        mUiBottomSheetDialog.setCancelable(!disable);
        mIBuilder.lockView(disable);
    }
}

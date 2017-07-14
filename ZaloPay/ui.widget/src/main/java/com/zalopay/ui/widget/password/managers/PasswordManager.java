package com.zalopay.ui.widget.password.managers;

import android.app.Activity;
import android.support.annotation.UiThread;
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

    @UiThread
    public synchronized void show() {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!isShowing()) {
                        mUiBottomSheetDialog.show();
                        mUiBottomSheetDialog.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @UiThread
    public synchronized boolean isShowing() {
        return mUiBottomSheetDialog != null && mUiBottomSheetDialog.isShowing();
    }

    @UiThread
    public synchronized void close() {
        if (mIBuilder == null) {
            return;
        }
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mUiBottomSheetDialog != null && isShowing()) {
                        mUiBottomSheetDialog.dismiss();
                        Timber.d("dismiss password popup");
                    }
                    Timber.d("release password popup");
                    mIBuilder = null;
                    mUiBottomSheetDialog = null;
                    mActivity = null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @UiThread
    public void setError(final String pMessage) {
        if (!TextUtils.isEmpty(pMessage) && mIBuilder != null) {
            try {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mIBuilder.showLoadding(false);
                        mIBuilder.setError(pMessage);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    @UiThread
    public void showLoading(final boolean pShowing) {
        if (mIBuilder == null) {
            Timber.d("mIBuilder is null");
            return;
        }
        try {
            getActivity().runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     mIBuilder.showLoadding(pShowing);
                 }
             });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @UiThread
    public void setTitle(final String pTitle) {
        if (mIBuilder == null) {
            Timber.d("mIBuilder is null");
            return;
        }
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mIBuilder.setTitle(pTitle);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void lock() {
        disable(true);
    }

    public void unlock() {
        disable(false);
    }

    @UiThread
    private void disable(final boolean disable) {
        if (mUiBottomSheetDialog == null) {
            Timber.d("mUiBottomSheetDialog is null");
            return;
        }
        try {
            getActivity().runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     mUiBottomSheetDialog.preventDrag(disable);
                     mUiBottomSheetDialog.setCancelable(!disable);
                     mIBuilder.lockView(disable);
                 }
             });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Activity getActivity() throws Exception {
        if (mActivity == null || mActivity.get() == null) {
            throw new IllegalAccessException("Activity is null");
        }
        return mActivity.get();
    }

}

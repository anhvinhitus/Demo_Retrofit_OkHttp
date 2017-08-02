package com.zalopay.ui.widget.password.managers;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.design.widget.BottomSheetBehavior;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.zalopay.ui.widget.UIBottomSheetDialog;
import com.zalopay.ui.widget.password.bottomsheet.PasswordViewRender;
import com.zalopay.ui.widget.password.interfaces.IBuilder;
import com.zalopay.ui.widget.password.interfaces.IControl;

import java.lang.ref.WeakReference;

import timber.log.Timber;

import static android.content.ContentValues.TAG;

/**
 * Created by lytm on 23/05/2017.
 */

public class PasswordManager {
    IBuilder mIBuilder;
    UIBottomSheetDialog mUiBottomSheetDialog;
    WeakReference<Activity> mActivity;
    private IControl mControl = new IControl() {
        @Override
        public void onClose() {
            try {
                if (mIBuilder != null && mIBuilder.getIFPinCallBack() != null) {
                    mIBuilder.getIFPinCallBack().onClose();
                }
                close();
            } catch (Exception e) {
                Timber.d("PasswordManager [%s]", e);
            }
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
        mUiBottomSheetDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public IBuilder getBuilder() {
        return mIBuilder;
    }

    @UiThread
    public synchronized void show() throws Exception {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isShowing()) {
                    try {
                        mUiBottomSheetDialog.show();
                        mUiBottomSheetDialog.setState(BottomSheetBehavior.STATE_EXPANDED);
                    } catch (Exception e) {
                        Timber.d(e);
                    }
                }
            }
        });
    }

    @UiThread
    public synchronized boolean isShowing() {
        return mUiBottomSheetDialog != null && mUiBottomSheetDialog.isShowing();
    }

    @UiThread
    public synchronized void close() throws Exception {
        if (mIBuilder == null) {
            return;
        }
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
    }

    @UiThread
    public void setError(final String pMessage) throws Exception {
        if (!TextUtils.isEmpty(pMessage) && mIBuilder != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mIBuilder.showLoadding(false);
                    mIBuilder.setError(pMessage);
                }
            });
        }
    }

    @UiThread
    public void showLoading(final boolean pShowing) throws Exception {
        if (mIBuilder == null) {
            Timber.d("mIBuilder is null");
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIBuilder.showLoadding(pShowing);
            }
        });
    }

    @UiThread
    public void setTitle(final String pTitle) throws Exception {
        if (mIBuilder == null) {
            Timber.d("mIBuilder is null");
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIBuilder.setTitle(pTitle);
            }
        });
    }

    @UiThread
    public void setNeedHashPassword(final boolean pNeedHashPass) throws Exception {
        if (mIBuilder == null) {
            Timber.d("mIBuilder is null");
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIBuilder.setNeedHashPass(pNeedHashPass);
            }
        });
    }

    @UiThread void resetPasswordInput() throws Exception {
        if (mIBuilder == null) {
            Timber.d("mIBuilder is null");
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIBuilder.resetPasswordInput();
            }
        });
    }

    @UiThread
    public void showOTPInputView() throws Exception {
        if (mIBuilder == null) {
            Timber.d("mIBuilder is null");
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIBuilder.showOTPInputView();
            }
        });
    }

    @UiThread
    public void setOTP(final String otp) throws Exception {
        if (mIBuilder == null) {
            Timber.d("mIBuilder is null");
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIBuilder.setOTPValue(otp);
            }
        });
    }

    @UiThread
    public void setViewDraggable(final boolean enable) throws Exception {
        if (mUiBottomSheetDialog == null) {
            Timber.d("mUiBottomSheetDialog is null");
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUiBottomSheetDialog.preventDrag(!enable);
            }
        });
    }

    @UiThread
    public void setCanTouchOutside(final boolean canTouch) throws Exception {
        if (mUiBottomSheetDialog == null) {
            Timber.d("mUiBottomSheetDialog is null");
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUiBottomSheetDialog.setCanceledOnTouchOutside(canTouch);
            }
        });
    }

    @UiThread
    public void lockElementView(final boolean disable) throws Exception {
        if (mUiBottomSheetDialog == null) {
            Timber.d("mUiBottomSheetDialog is null");
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIBuilder.lockView(disable);
            }
        });
    }

    public void lock() throws Exception {
        disable(true);
    }

    public void unlock() throws Exception {
        disable(false);
    }

    @UiThread
    private void disable(final boolean disable) throws Exception {
        if (mUiBottomSheetDialog == null) {
            Timber.d("mUiBottomSheetDialog is null");
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUiBottomSheetDialog.preventDrag(disable);
                mUiBottomSheetDialog.setCancelable(!disable);
                mIBuilder.lockView(disable);
            }
        });
    }

    private Activity getActivity() throws Exception {
        if (mActivity == null || mActivity.get() == null) {
            throw new IllegalAccessException("Activity is null");
        }
        return mActivity.get();
    }

}

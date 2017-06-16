package com.zalopay.ui.widget.pinlayout.managers;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.zalopay.ui.widget.UIBottomSheetDialog;
import com.zalopay.ui.widget.pinlayout.bottomsheet.PinViewRender;
import com.zalopay.ui.widget.pinlayout.interfaces.IBuilder;
import com.zalopay.ui.widget.pinlayout.interfaces.IFControl;
import com.zalopay.ui.widget.pinlayout.interfaces.IFPinCallBack;

import java.lang.ref.WeakReference;

/**
 * Created by lytm on 23/05/2017.
 */

public class PinManager {
    private WeakReference<IFPinCallBack> mIPinCloseCallBack;
    private WeakReference<Activity> mActivity;
    private IBuilder mIBuilder;
    private UIBottomSheetDialog mUiBottomSheetDialog;
    private IFControl Control = new IFControl() {
        @Override
        public void clickCancel() {
            closePinView();
        }
    };

    /**
     * @param pActivity
     * @param pTitle
     * @param pLogoPath
     * @param pIPinCloseCallBack
     */
    public PinManager(Activity pActivity, String pTitle, String pLogoPath, IFPinCallBack pIPinCloseCallBack) {
        mIPinCloseCallBack = new WeakReference<>(pIPinCloseCallBack);
        mActivity = new WeakReference<>(pActivity);
        View contentView = View.inflate(mActivity.get(), com.zalopay.ui.widget.R.layout.view_pin_code, null);
        mIBuilder = PinViewRender.getBuilder()
                .setView(contentView)
                .setIFPinCallBack(mIPinCloseCallBack.get())
                .setIFControl(Control)
                .setTitle(pTitle)
                .setLogoPath(pLogoPath);
        mUiBottomSheetDialog = new UIBottomSheetDialog(mActivity.get(), com.zalopay.ui.widget.R.style.CoffeeDialog, mIBuilder.build());
    }

    public void setState(int pState) {
        if (mUiBottomSheetDialog != null) {
            mUiBottomSheetDialog.setState(pState);
        }
    }

    public void showPinView() {
        if (mUiBottomSheetDialog != null && !isShowingPin()) {
            mUiBottomSheetDialog.show();
        }
    }

    public boolean isShowingPin() {
        return mUiBottomSheetDialog.isShowing();
    }

    public void closePinView() {
        if (mUiBottomSheetDialog != null && isShowingPin() && mIBuilder != null) {
            mIBuilder.showLoadding(false);
            mIBuilder.clearText();
            mIBuilder.getIFPinCallBack().onCancel();
            mUiBottomSheetDialog.dismiss();
        }
        /*mActivity = null;
        mIPinCloseCallBack = null;
        mIBuilder = null;
        mUiBottomSheetDialog = null;*/
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
}

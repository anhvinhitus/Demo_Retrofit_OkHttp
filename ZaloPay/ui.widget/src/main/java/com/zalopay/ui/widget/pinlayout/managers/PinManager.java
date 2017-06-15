package com.zalopay.ui.widget.pinlayout.managers;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;

import com.zalopay.ui.widget.UIBottomSheetDialog;
import com.zalopay.ui.widget.pinlayout.bottomsheet.PinViewRender;
import com.zalopay.ui.widget.pinlayout.interfaces.IBuilder;
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

    /**
     * @param pActivity
     * @param pTitle
     * @param pIdImage
     * @param pIPinCloseCallBack
     */
    public PinManager(Activity pActivity, String pTitle, String pIdImage, IFPinCallBack pIPinCloseCallBack) {
        mIPinCloseCallBack = new WeakReference<>(pIPinCloseCallBack);
        mActivity = new WeakReference<>(pActivity);
        View contentView = View.inflate(mActivity.get(), com.zalopay.ui.widget.R.layout.view_pin_code, null);
        mIBuilder = PinViewRender.getBuilder()
                .setView(contentView)
                .setIFPinCallBack(mIPinCloseCallBack.get())
                .setTitle(pTitle)
                .setLogoPath(pIdImage);
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
            mUiBottomSheetDialog.dismiss();
        }
        mActivity = null;
        mIPinCloseCallBack = null;
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

    /**
     * @param pIdLogo
     * @param pContent
     */
    public void setContent(String pIdLogo, String pContent) {
        if (!TextUtils.isEmpty(pContent) && mIBuilder != null) {
            mIBuilder.setLogoPath(pIdLogo);
            mIBuilder.setTitle(pContent);
        }
    }
}

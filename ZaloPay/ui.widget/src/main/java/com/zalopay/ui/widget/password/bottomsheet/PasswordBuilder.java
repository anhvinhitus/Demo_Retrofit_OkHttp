package com.zalopay.ui.widget.password.bottomsheet;


import android.app.Activity;
import android.text.TextUtils;
import android.view.View;

import com.zalopay.ui.widget.UIBottomSheetDialog;
import com.zalopay.ui.widget.password.interfaces.IBuilder;
import com.zalopay.ui.widget.password.interfaces.IControl;
import com.zalopay.ui.widget.password.interfaces.IPinCallBack;
import com.zalopay.ui.widget.password.interfaces.ISetDataToView;

public abstract class PasswordBuilder implements IBuilder {
    protected IPinCallBack mIPinCallBack;
    protected View mView;
    protected Activity mActivity;
    protected String mIdLogo;
    protected String mTextTitle;
    protected ISetDataToView mISetDataToView;
    protected IControl mControl;
    protected boolean mFingerPrint;

    public UIBottomSheetDialog.IRender build() {
        return new PasswordViewRender(this);
    }

    @Override
    public IBuilder setIFPinCallBack(IPinCallBack pinListener) {
        mIPinCallBack = pinListener;
        return this;
    }

    @Override
    public IPinCallBack getIFPinCallBack() {
        return mIPinCallBack;
    }

    @Override
    public View getView() {
        return mView;
    }

    @Override
    public IBuilder setView(View pView) {
        this.mView = pView;
        return this;
    }

    @Override
    public void release() {
        mIPinCallBack = null;
        mView = null;
    }

    @Override
    public IBuilder setErrorMessage(Activity pActivity, String pMessage) {
        mActivity = pActivity;
        mISetDataToView.setErrorMessage(pActivity, pMessage);
        return this;
    }

    @Override
    public void getCallBackToView(ISetDataToView pIFDataToView) {
        mISetDataToView = pIFDataToView;

    }

    @Override
    public IBuilder clearText() {
        if (mISetDataToView != null) {
            mISetDataToView.clearData();
        }
        return this;
    }

    @Override
    public IBuilder setTitle(String pTitle) {
        mTextTitle = pTitle;
        if (!TextUtils.isEmpty(pTitle) && mISetDataToView != null) {
            mISetDataToView.setTitle(pTitle);
        }
        return this;
    }

    @Override
    public IBuilder setLogoPath(String pIdImage) {
        mIdLogo = pIdImage;
        if (!TextUtils.isEmpty(pIdImage) && mISetDataToView != null) {
            mISetDataToView.setImage(pIdImage);
        }
        return this;
    }

    @Override
    public IBuilder showLoadding(boolean pShow) {
        if (mISetDataToView != null) {
            mISetDataToView.showLoading(pShow);
        }
        return this;
    }


    @Override
    public String getLogoPath() {
        return mIdLogo;
    }

    @Override
    public String getTitle() {
        return mTextTitle;
    }

    @Override
    public IBuilder setIFControl(IControl control) {
        mControl = control;
        return this;
    }

    @Override
    public IControl getIFControl() {
        return mControl;
    }

    @Override
    public IBuilder setFingerPrint(boolean pShow) {
        if (mISetDataToView != null) {
            mISetDataToView.showFingerPrintCheckBox(pShow);
        }
        mFingerPrint = pShow;
        return this;
    }

    @Override
    public boolean getFingerPrint() {
        return mFingerPrint;
    }

    @Override
    public IBuilder setLockControl(boolean isLockControl) {
        if (mISetDataToView != null) {
            mISetDataToView.lockControl(isLockControl);
        }
        return this;
    }
}

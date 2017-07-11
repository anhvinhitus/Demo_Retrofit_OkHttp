package com.zalopay.ui.widget.password.bottomsheet;


import android.text.TextUtils;
import android.view.View;

import com.zalopay.ui.widget.UIBottomSheetDialog;
import com.zalopay.ui.widget.password.interfaces.IBuilder;
import com.zalopay.ui.widget.password.interfaces.IControl;
import com.zalopay.ui.widget.password.interfaces.IPasswordCallBack;
import com.zalopay.ui.widget.password.interfaces.ISetDataToView;

public abstract class PasswordBuilder implements IBuilder {
    protected IPasswordCallBack mIPinCallBack;
    protected View mView;
    protected String mIdLogo;
    protected String mTextPmcName;
    protected ISetDataToView mISetDataToView;
    protected IControl mControl;
    protected boolean mFingerPrint;
    protected String mTitle;

    public UIBottomSheetDialog.IRender build() {
        return new PasswordViewRender(this);
    }

    @Override
    public IBuilder setPasswordCallBack(IPasswordCallBack pinListener) {
        mIPinCallBack = pinListener;
        return this;
    }

    @Override
    public IPasswordCallBack getIFPinCallBack() {
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
    public IBuilder setError(String pMessage) {
        mISetDataToView.setErrorMessage(pMessage);
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
    public IBuilder setContent(String pPmcName) {
        mTextPmcName = pPmcName;
        if (!TextUtils.isEmpty(pPmcName) && mISetDataToView != null) {
            mISetDataToView.setContent(pPmcName);
        }
        return this;
    }

    @Override
    public IBuilder setLogoPath(String pIdImage) {
        mIdLogo = pIdImage;
        if (!TextUtils.isEmpty(pIdImage) && mISetDataToView != null) {
            mISetDataToView.setImagePath(pIdImage);
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
    public String getPmcName() {
        return mTextPmcName;
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
    public IBuilder showFPSuggestCheckBox(boolean pShow) {
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
    public IBuilder lockView(boolean isLock) {
        if (mISetDataToView != null) {
            mISetDataToView.lockView(isLock);
        }
        return this;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public IBuilder setTitle(String pTitle) {
        mTitle = pTitle;
        if (mISetDataToView != null) {
            mISetDataToView.setTitle(pTitle);
        }
        return this;
    }
}

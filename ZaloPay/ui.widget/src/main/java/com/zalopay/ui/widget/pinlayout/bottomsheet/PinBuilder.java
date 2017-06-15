package com.zalopay.ui.widget.pinlayout.bottomsheet;


import android.app.Activity;
import android.view.View;

import com.zalopay.ui.widget.UIBottomSheetDialog;
import com.zalopay.ui.widget.pinlayout.interfaces.IBuilder;
import com.zalopay.ui.widget.pinlayout.interfaces.IFControl;
import com.zalopay.ui.widget.pinlayout.interfaces.IFPinCallBack;
import com.zalopay.ui.widget.pinlayout.interfaces.IFSetDataToView;

public abstract class PinBuilder implements IBuilder {
    protected IFPinCallBack mIfPinCallBack;
    protected View mView;
    protected Activity mActivity;
    protected String mIdLogo;
    protected String mTextContent;
    protected IFSetDataToView mIFSetDataToView;
    protected IFControl mControl;

    public UIBottomSheetDialog.IRender build() {
        return new PinViewRender(this);
    }

    @Override
    public IBuilder setIFPinCallBack(IFPinCallBack pinListener) {
        mIfPinCallBack = pinListener;
        return this;
    }

    @Override
    public IFPinCallBack getIFPinCallBack() {
        return mIfPinCallBack;
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
        mIfPinCallBack = null;
        mView = null;
    }

    @Override
    public IBuilder setErrorMessage(Activity pActivity, String pMessage) {
        mActivity = pActivity;
        mIFSetDataToView.setErrorMessage(pActivity, pMessage);
        return this;
    }

    @Override
    public void getCallBackToView(IFSetDataToView pIFDataToView) {
        mIFSetDataToView = pIFDataToView;

    }

    @Override
    public IBuilder clearText() {
        if (mIFSetDataToView != null) {
            mIFSetDataToView.clearData();
        }
        return this;
    }

    @Override
    public IBuilder setTitle(String pTitle) {
        mTextContent = pTitle;
        return this;
    }

    @Override
    public IBuilder setLogoPath(String pIdImage) {
        mIdLogo = pIdImage;
        return this;
    }

    @Override
    public IBuilder showLoadding(boolean pShow) {
        if (mIFSetDataToView != null) {
            mIFSetDataToView.showLoading(pShow);
        }
        return this;
    }


    @Override
    public String getLogoPath() {
        return mIdLogo;
    }

    @Override
    public String getTextContent() {
        return mTextContent;
    }

    @Override
    public IBuilder setIFControl(IFControl control) {
        mControl = control;
        return this;
    }

    @Override
    public IFControl getIFControl() {
        return mControl;
    }
}

package com.zalopay.ui.widget.password.bottomsheet;


import android.text.TextUtils;
import android.view.View;

import com.zalopay.ui.widget.UIBottomSheetDialog;
import com.zalopay.ui.widget.password.interfaces.IBuilder;
import com.zalopay.ui.widget.password.interfaces.IControl;
import com.zalopay.ui.widget.password.interfaces.IPasswordCallBack;
import com.zalopay.ui.widget.password.interfaces.ISetDataToView;
import com.zalopay.ui.widget.password.interfaces.OnCallSupportListener;

public abstract class PasswordBuilder implements IBuilder {
    protected IPasswordCallBack mIPinCallBack;
    protected OnCallSupportListener mCallSupportListener;
    protected View mView;
    protected String mIdLogo;
    protected String mTextPmcName;
    protected ISetDataToView mISetDataToView;
    protected IControl mControl;
    protected boolean mFingerPrint;
    protected boolean isSupportInfoViewVisible;
    protected String mTitle;
    protected boolean mNeedHashPass = true;
    protected boolean isConfirmClose = false;
    protected int mNumberOfTimesWrongPass = 3;

    public UIBottomSheetDialog.IRender build() {
        return new PasswordViewRender(this);
    }

    @Override
    public IBuilder setNeedHashPass(boolean needHash) {
        mNeedHashPass = needHash;
        return this;
    }

    @Override
    public boolean needHashPass() {
        return mNeedHashPass;
    }

    @Override
    public IBuilder setPasswordCallBack(IPasswordCallBack pinListener) {
        mIPinCallBack = pinListener;
        return this;
    }

    @Override
    public IBuilder setOnCallSupportListener(OnCallSupportListener callSupportListener) {
        mCallSupportListener = callSupportListener;
        return this;
    }

    @Override
    public IPasswordCallBack getIFPinCallBack() {
        return mIPinCallBack;
    }

    public OnCallSupportListener getCallSupportListener() {
        return mCallSupportListener;
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
    public IBuilder showSupportInfo(boolean pShow) {
        if (mISetDataToView != null) {
            mISetDataToView.showSupportInfo(pShow);
        }
        isSupportInfoViewVisible = pShow;
        return this;
    }

    @Override
    public IBuilder resetPasswordInput() {
        if (mISetDataToView != null) {
            mISetDataToView.resetPasswordInput();
        }
        return this;
    }

    @Override
    public IBuilder showOTPInputView() {
        if (mISetDataToView != null) {
            mISetDataToView.showOTPInputView();
        }
        return this;
    }

    @Override
    public IBuilder setOTPValue(String otpValue) {
        if (mISetDataToView != null) {
            mISetDataToView.setOTPValue(otpValue);
        }
        return this;
    }

    @Override
    public IBuilder setConfirmClose(boolean confirmClose) {
        isConfirmClose = confirmClose;
        return this;
    }

    @Override
    public boolean isConfirmClose() {
        return isConfirmClose;
    }

    @Override
    public boolean getFingerPrint() {
        return mFingerPrint;
    }

    @Override
    public boolean isSupportInfoVisible() {
        return isSupportInfoViewVisible;
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

    @Override
    public IBuilder resetOTPContent() {
        if (mISetDataToView != null) {
            mISetDataToView.resetOTPContent();
        }
        return this;
    }

    @Override
    public IBuilder setMaxNumberOfTimesWrongPass(int pNumber) {
        mNumberOfTimesWrongPass = pNumber;
        return this;
    }

    @Override
    public int getMaxNumberOfTimesWrongPass() {
        return mNumberOfTimesWrongPass;
    }
}

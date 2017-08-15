package com.zalopay.ui.widget.password.interfaces;

import android.view.View;

import com.zalopay.ui.widget.UIBottomSheetDialog;

public interface IBuilder {

    IBuilder setNeedHashPass(boolean needHash);

    IBuilder setMaxNumberOfTimesWrongPass(int pNumber);

    int getMaxNumberOfTimesWrongPass();

    boolean needHashPass();

    IBuilder setPasswordCallBack(IPasswordCallBack pinListener);

    IBuilder setOnCallSupportListener(OnCallSupportListener callSupportListener);

    IPasswordCallBack getIFPinCallBack();

    OnCallSupportListener getCallSupportListener();

    View getView();

    IBuilder setView(View pView);

    String getLogoPath();

    IBuilder setLogoPath(String pIdImage);

    String getPmcName();

    String getTitle();

    IBuilder setTitle(String pTitle);

    IBuilder setError(String pMessage);

    IBuilder clearText();

    IBuilder setContent(String pPmcName);

    IBuilder showLoadding(boolean pShow);

    IBuilder showFPSuggestCheckBox(boolean pShow);

    IBuilder showSupportInfo(boolean pShow);

    IBuilder resetPasswordInput();

    IBuilder showOTPInputView();

    IBuilder setOTPValue(String otpValue);

    IBuilder setConfirmClose(boolean confirmed);

    boolean isConfirmClose();

    boolean getFingerPrint();

    boolean isSupportInfoVisible();

    void getCallBackToView(ISetDataToView pIfError);

    UIBottomSheetDialog.IRender build();

    IControl getIFControl();

    IBuilder setIFControl(IControl control);

    IBuilder lockView(boolean isLockControl);

    IBuilder resetOTPContent();

    void release();
}

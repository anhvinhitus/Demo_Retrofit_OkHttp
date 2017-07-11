package com.zalopay.ui.widget.password.interfaces;

import android.view.View;

import com.zalopay.ui.widget.UIBottomSheetDialog;

public interface IBuilder {

    IBuilder setPasswordCallBack(IPasswordCallBack pinListener);

    IPasswordCallBack getIFPinCallBack();

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

    boolean getFingerPrint();

    void getCallBackToView(ISetDataToView pIfError);

    UIBottomSheetDialog.IRender build();

    IControl getIFControl();

    IBuilder setIFControl(IControl control);

    IBuilder lockView(boolean isLockControl);

    void release();
}

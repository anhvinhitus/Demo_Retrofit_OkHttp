package com.zalopay.ui.widget.password.interfaces;

import android.app.Activity;
import android.view.View;

import com.zalopay.ui.widget.UIBottomSheetDialog;

public interface IBuilder {

    IBuilder setIFPinCallBack(IPinCallBack pinListener);

    IPinCallBack getIFPinCallBack();

    View getView();

    String getLogoPath();

    String getPmcName();

    String getTitle();

    IBuilder setView(View pView);

    IBuilder setErrorMessage(Activity pActivity, String pMessage);

    IBuilder clearText();

    IBuilder setPmName(String pPmcName);

    IBuilder setTitle(String pTitle);

    IBuilder setLogoPath(String pIdImage);

    IBuilder showLoadding(boolean pShow);

    IBuilder setFingerPrint(boolean pShow);

    boolean getFingerPrint();

    void getCallBackToView(ISetDataToView pIfError);

    UIBottomSheetDialog.IRender build();

    IBuilder setIFControl(IControl control);

    IControl getIFControl();

    IBuilder setLockControl(boolean isLockControl);

    void release();
}

package com.zalopay.ui.widget.password.interfaces;

import android.app.Activity;
import android.view.View;

import com.zalopay.ui.widget.UIBottomSheetDialog;

public interface IBuilder {

    IBuilder setIFPinCallBack(IPinCallBack pinListener);

    IPinCallBack getIFPinCallBack();

    View getView();

    String getLogoPath();

    String getTitle();

    IBuilder setView(View pView);

    IBuilder setErrorMessage(Activity pActivity, String pMessage);

    IBuilder clearText();

    IBuilder setTitle(String pTitle);

    IBuilder setLogoPath(String pIdImage);

    IBuilder showLoadding(boolean pShow);

    IBuilder setFingerPrint(boolean pShow);

    boolean getFingerPrint();

    void getCallBackToView(ISetDataToView pIfError);

    UIBottomSheetDialog.IRender build();

    IBuilder setIFControl(IControl control);

    IControl getIFControl();

    void release();
}

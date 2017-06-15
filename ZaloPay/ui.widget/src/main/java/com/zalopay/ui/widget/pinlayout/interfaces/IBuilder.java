package com.zalopay.ui.widget.pinlayout.interfaces;

import android.app.Activity;
import android.view.View;

import com.zalopay.ui.widget.UIBottomSheetDialog;

public interface IBuilder {

    IBuilder setIFPinCallBack(IFPinCallBack pinListener);

    IFPinCallBack getIFPinCallBack();

    View getView();

    String getLogoPath();

    String getTextContent();

    IBuilder setView(View pView);

    IBuilder setErrorMessage(Activity pActivity, String pMessage);

    IBuilder clearText();

    IBuilder setTitle(String pTitle);

    IBuilder setLogoPath(String pIdImage);

    IBuilder showLoadding(boolean pShow);

    void getCallBackToView(IFSetDataToView pIfError);

    UIBottomSheetDialog.IRender build();

    void release();
}

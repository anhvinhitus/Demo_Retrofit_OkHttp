package com.zalopay.ui.widget.password.interfaces;

/**
 * Created by lytm on 07/06/2017.
 */

public interface ISetDataToView {
    void setErrorMessage(String pError);

    void setImagePath(String pIdImage);

    void setContent(String pPmcName);

    void setTitle(String pTitle);

    void clearData();

    void showLoading(boolean pShow);

    void showFingerPrintCheckBox(boolean pShow);

    void showSupportInfo(boolean pShow);

    void lockView(boolean islock);

    void resetPasswordInput();

    void showOTPInputView();
}
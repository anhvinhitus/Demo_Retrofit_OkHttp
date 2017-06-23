package com.zalopay.ui.widget.password.interfaces;

import android.app.Activity;

/**
 * Created by lytm on 07/06/2017.
 */

public interface ISetDataToView {
    void setErrorMessage(Activity pActivity, String pError);

    void setImage(String pIdImage);

    void setPmcName(String pPmcName);

    void clearData();

    void showLoading(boolean pShow);

    void showFingerPrintCheckBox(boolean pShow);

    void lockControl(boolean islock);
}
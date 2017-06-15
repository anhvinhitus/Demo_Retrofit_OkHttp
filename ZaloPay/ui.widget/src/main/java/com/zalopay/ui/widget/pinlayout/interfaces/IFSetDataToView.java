package com.zalopay.ui.widget.pinlayout.interfaces;

import android.app.Activity;

/**
 * Created by lytm on 07/06/2017.
 */

public interface IFSetDataToView {
    void setErrorMessage(Activity pActivity, String pError);
    void setImage(int pIdImage);
    void setTitle(String pTitle);
    void clearData();
    void showLoading(boolean pShow);

}
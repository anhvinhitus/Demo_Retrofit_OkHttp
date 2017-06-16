package com.zalopay.ui.widget.password.interfaces;


/**
 * Created by lytm on 17/05/2017.
 */

public interface IPinCallBack {

    void onError(String pError);

    void onCheckedFingerPrint(boolean pChecked);

    void onCancel();

    void onComplete(String pHashPin);
}

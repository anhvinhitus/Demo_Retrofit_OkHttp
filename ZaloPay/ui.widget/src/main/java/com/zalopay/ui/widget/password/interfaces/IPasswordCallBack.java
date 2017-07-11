package com.zalopay.ui.widget.password.interfaces;


/**
 * Created by lytm on 17/05/2017.
 */

public interface IPasswordCallBack {

    void onError(String pError);

    void onCheckedFingerPrint(boolean pChecked);

    void onClose();

    void onComplete(String pHashPin);
}

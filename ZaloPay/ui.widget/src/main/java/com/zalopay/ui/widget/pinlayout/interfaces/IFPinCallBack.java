package com.zalopay.ui.widget.pinlayout.interfaces;


/**
 * Created by lytm on 17/05/2017.
 */

public interface IFPinCallBack {
    void onError(String pError);

    void onCheckedFingerPrint(boolean pChecked);

    void onCancel();


    void onComplete(String pHashPin);


}

package vn.com.vng.zalopay.react.listener;


import com.facebook.react.bridge.Promise;

import vn.com.vng.zalopay.react.Helpers;
import vn.com.zalopay.wallet.listener.ZPWOnSweetDialogListener;

/**
 * Created by longlv on 12/6/16.
 * Simple dialog event handler that will resolve to index value when user clicks OK
 */
public class SweetDialogSimpleEventListener implements ZPWOnSweetDialogListener {

    private Promise mPromise;
    private int mIndex;

    public SweetDialogSimpleEventListener(Promise promise, int index) {
        mPromise = promise;
        mIndex = index;
    }

    @Override
    public void onClickDiaLog(int i) {
        if (mPromise == null) {
            return;
        }
        Helpers.promiseResolve(mPromise, mIndex);
    }
}

package vn.com.zalopay.wallet.dialog;

/**
 * Created by lytm on 17/07/2017.
 */

public interface ZPWResultCallBackListener {

    void onResultOk(int pReturnCode, int pData);

    void onCancel(int pReturnCode);
}

package vn.com.vng.zalopay.mdl.redpackage;

import android.os.Bundle;

/**
 * Created by longlv on 19/07/2016.
 * Listener of IRedPackagePayService
 */
public interface IRedPackagePayListener {
    void onParameterError(String param);

    void onResponseError(int status);

    void onResponseSuccess(Bundle bundle);

    void onResponseTokenInvalid();

    void onResponseCancel();

    void onNotEnoughMoney();
}

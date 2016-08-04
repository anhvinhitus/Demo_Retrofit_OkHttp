package vn.com.vng.zalopay.react.redpacket;

import android.os.Bundle;

/**
 * Created by longlv on 19/07/2016.
 * Listener of IRedPacketPayService
 */
public interface RedPacketPayListener {
    void onParameterError(String param);

    void onResponseError(int status);

    void onResponseSuccess(Bundle bundle);

    void onResponseTokenInvalid();

    void onResponseCancel();

    void onNotEnoughMoney();
}

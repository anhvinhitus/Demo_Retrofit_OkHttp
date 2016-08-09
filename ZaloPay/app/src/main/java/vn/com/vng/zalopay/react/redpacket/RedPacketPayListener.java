package vn.com.vng.zalopay.react.redpacket;

import android.os.Bundle;

import vn.com.vng.zalopay.react.error.PaymentError;

/**
 * Created by longlv on 19/07/2016.
 * Listener of IRedPacketPayService
 */
public interface RedPacketPayListener {
    void onParameterError(String param);

    void onResponseError(PaymentError paymentError);

    void onResponseSuccess(Bundle bundle);

    void onResponseTokenInvalid();

    void onAppError(String msg);

    void onNotEnoughMoney();
}

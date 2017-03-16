package vn.com.vng.zalopay.data.paymentconnector;

import android.support.annotation.NonNull;

import java.io.IOException;

import vn.com.vng.zalopay.data.ws.model.PaymentRequestData;

/**
 * Created by hieuvm on 3/9/17.
 */

interface PaymentConnectorCallback {

    void onStart();

    void onResponse(@NonNull PaymentRequestData data);

    void onFailure(IOException e);
}

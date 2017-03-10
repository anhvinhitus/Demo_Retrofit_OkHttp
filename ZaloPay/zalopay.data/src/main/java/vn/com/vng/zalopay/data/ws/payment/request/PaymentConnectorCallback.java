package vn.com.vng.zalopay.data.ws.payment.request;

import java.io.IOException;

import vn.com.vng.zalopay.data.ws.model.PaymentRequestData;

/**
 * Created by hieuvm on 3/9/17.
 */

public interface PaymentConnectorCallback {
    void onResult(PaymentRequestData data);

    void onFailure(IOException e);
}

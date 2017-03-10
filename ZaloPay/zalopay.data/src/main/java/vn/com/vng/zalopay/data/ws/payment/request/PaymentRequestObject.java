package vn.com.vng.zalopay.data.ws.payment.request;

import android.support.annotation.NonNull;

/**
 * Created by hieuvm on 3/9/17.
 */

public class PaymentRequestObject {

    public PaymentRequest request;
    public PaymentRequestCallback callback;

    public PaymentRequestObject(@NonNull PaymentRequest request, PaymentRequestCallback callback) {
        this.request = request;
        this.callback = callback;
    }

    public void cancel() {
      
    }
}

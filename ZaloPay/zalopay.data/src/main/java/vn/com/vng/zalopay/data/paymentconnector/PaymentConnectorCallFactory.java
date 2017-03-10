package vn.com.vng.zalopay.data.paymentconnector;

import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by hieuvm on 3/9/17.
 * Call.Factory for creating PaymentConnectorCall
 */

public class PaymentConnectorCallFactory implements Call.Factory {

    private final PaymentConnectorService mConnectorService;

    public PaymentConnectorCallFactory(PaymentConnectorService connectorService) {
        this.mConnectorService = connectorService;
    }

    @Override
    public Call newCall(Request request) {
        return new PaymentConnectorCall(mConnectorService, request);
    }
}

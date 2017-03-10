package vn.com.vng.zalopay.data.ws.paymentconnector;

import vn.com.vng.zalopay.data.Constants;

/**
 * Created by hieuvm on 3/9/17.
 */

public class PaymentConnectorFactory {

    public static final PaymentRequest createBalanceRequest(String zalopayId, String token) {
        PaymentRequest.Builder builder = new PaymentRequest.Builder()
                .domain("sandbox.zalopay.com.vn")
                .path(Constants.TPE_API.GETBALANCE)
                .addParam("userid", zalopayId)
                .addParam("accesstoken", token)
                ;
        return builder.build();
    }
}

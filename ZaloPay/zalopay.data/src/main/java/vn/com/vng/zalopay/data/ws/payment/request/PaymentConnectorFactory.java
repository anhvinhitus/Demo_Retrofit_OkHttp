package vn.com.vng.zalopay.data.ws.payment.request;

import okhttp3.Request;
import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;

/**
 * Created by hieuvm on 3/9/17.
 */

public class PaymentConnectorFactory {

    public static PaymentRequest createBalanceRequest(String zalopayId, String token) {
        PaymentRequest.Builder builder = new PaymentRequest.Builder()
                .domain("sandbox.zalopay.com.vn")
                .path(Constants.TPE_API.GETBALANCE)
                .addParam("userid", zalopayId)
                .addParam("accesstoken", token);
        return builder.build();
    }

    public static PaymentRequest createRequest(Request originalRequest) {

        Timber.d("convert: %s %s", originalRequest, originalRequest != null ? originalRequest.url() : "Url empty");
        PaymentRequest.Builder builder = new PaymentRequest.Builder()
                .domain(originalRequest.url().host())
                .path(Constants.TPE_API.GETBALANCE);
        return builder.build();
    }
}

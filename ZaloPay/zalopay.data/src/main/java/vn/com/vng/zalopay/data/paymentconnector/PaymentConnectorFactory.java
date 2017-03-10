package vn.com.vng.zalopay.data.paymentconnector;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;
import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;

/**
 * Created by hieuvm on 3/9/17.
 * Factory for creating PaymentRequest from OkHttp Request
 */

class PaymentConnectorFactory {

    static PaymentRequest createRequest(Request originalRequest) {

        if (originalRequest == null || originalRequest.url() == null) {
            return null;
        }

        Timber.d("Creating PaymentRequest from OkHttp request: %s", originalRequest);
        PaymentRequest.Builder builder = new PaymentRequest.Builder()
                .domain(originalRequest.url().host())
                .path(Constants.TPE_API.GETBALANCE);

        List<Pair<String, String>> query = new ArrayList<>();
        int querySize = originalRequest.url().querySize();
        for (int index = 0; index < querySize; index ++) {
            Pair<String, String> pair = new Pair<>(originalRequest.url().queryParameterName(index), originalRequest.url().queryParameterValue(index));
            query.add(pair);
        }
        builder.params(query);

        // todo: add/build params from request's body
        builder.path(originalRequest.url().encodedPath());
        return builder.build();
    }
}

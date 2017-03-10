package vn.com.vng.zalopay.data.paymentconnector;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.HttpUrl;
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

        HttpUrl httpUrl = originalRequest.url();

        PaymentRequest.Builder builder = new PaymentRequest.Builder()
                .domain(httpUrl.host());

        List<Pair<String, String>> query = new ArrayList<>();
        int querySize = originalRequest.url().querySize();
        for (int index = 0; index < querySize; index++) {
            Pair<String, String> pair = new Pair<>(httpUrl.queryParameterName(index), httpUrl.queryParameterValue(index));
            query.add(pair);
        }
        builder.params(query);

        Headers headers = originalRequest.headers();
        if (headers != null) {
            int headerSize = headers.size();
            List<Pair<String, String>> header = new ArrayList<>();
            for (int i = 0; i < headerSize; i++) {
                Pair<String, String> pair = new Pair<>(headers.name(i), headers.value(i));
                header.add(pair);
            }

            builder.headers(header);
        }

        builder.port(httpUrl.port());
        builder.path(httpUrl.encodedPath());

        return builder.build();
    }
}

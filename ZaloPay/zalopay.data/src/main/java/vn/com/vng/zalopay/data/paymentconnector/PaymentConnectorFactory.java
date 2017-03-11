package vn.com.vng.zalopay.data.paymentconnector;

import android.support.annotation.NonNull;
import android.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;
import timber.log.Timber;

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
                .domain(httpUrl.host())
                .method(originalRequest.method());

        List<Pair<String, String>> query = getRequestParams(httpUrl);
        if (originalRequest.method().equalsIgnoreCase("POST")) {
            try {
                RequestBody body = originalRequest.body();
                Timber.d("Request body: %s, length: %s, contentType: %s", body, body.contentLength(), body.contentType());
                final Buffer buffer = new Buffer();
                body.writeTo(buffer);
                String bodyContent = buffer.readUtf8();
                Timber.d("Request body: %s", bodyContent);

                HttpUrl newUrl = HttpUrl.parse("http://dummy/?" + bodyContent);
                if (newUrl != null) {
                    List<Pair<String, String>> query1 = getRequestParams(newUrl);
                    query.addAll(query1);
                }
            } catch (IOException e) {
                Timber.d(e);
            }
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

    @NonNull
    private static List<Pair<String, String>> getRequestParams(HttpUrl requestUrl) {
        List<Pair<String, String>> params = new ArrayList<>();
        int querySize = requestUrl.querySize();
        for (int index = 0; index < querySize; index++) {
            Pair<String, String> pair = new Pair<>(requestUrl.queryParameterName(index), requestUrl.queryParameterValue(index));
            params.add(pair);
        }
        return params;
    }
}

package vn.com.vng.zalopay.data.paymentconnector;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hieuvm on 3/8/17.
 */

class PaymentRequest {

    private static long sPaymentRequestId = System.currentTimeMillis();

    private static long generateRequestId() {
        // this.id = UUID.randomUUID().toString();
        return sPaymentRequestId++;
    }

    public int priority;
    public boolean cancelled;

    public long requestId;
    public List<Pair<String, String>> params;
    public List<Pair<String, String>> headers;
    public String path;
    public int port;
    public String method;
    public String domain;
    public long sentRequestAtMillis;

    public PaymentRequest(long requestId, String domain, String path, List<Pair<String, String>> params, List<Pair<String, String>> headers, int port, String method, int priority) {
        this.priority = priority;
        this.requestId = requestId;
        this.params = params;
        this.headers = headers;
        this.path = path;
        this.port = port;
        this.method = method;
        this.domain = domain;
        this.sentRequestAtMillis = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof PaymentRequest) {
            return requestId == ((PaymentRequest) o).requestId;
        }

        return false;
    }

    public static final class Builder {

        public int priority;

        private List<Pair<String, String>> params;
        private List<Pair<String, String>> headers;
        private String path;
        private int port;
        private String method;
        private String domain;

        public Builder() {
        }

        public PaymentRequest.Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public PaymentRequest.Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public PaymentRequest.Builder method(String method) {
            this.method = method;
            return this;
        }

        public PaymentRequest.Builder port(int port) {
            this.port = port;
            return this;
        }

        public PaymentRequest.Builder path(String path) {
            this.path = path;
            return this;
        }

        public PaymentRequest.Builder params(List<Pair<String, String>> params) {
            this.params = params;
            return this;
        }

        public PaymentRequest.Builder headers(List<Pair<String, String>> headers) {
            this.headers = headers;
            return this;
        }

        public PaymentRequest.Builder addParam(@NonNull String key, @NonNull String value) {
            if (this.params == null) {
                this.params = new ArrayList<>();
            }
            Pair<String, String> param = new Pair<>(key, value);
            this.params.add(param);
            return this;
        }

        /**
         * build params default
         */

        private List<Pair<String, String>> buildParams() {
            if (this.params == null) {
                this.params = new ArrayList<>();
            }

            return params;
        }

        public PaymentRequest build() {
            if (TextUtils.isEmpty(domain) || params == null) {
                //throw exception
            }

            return new PaymentRequest(generateRequestId(), domain, path, buildParams(),
                    headers, port, method, priority);
        }
    }

}

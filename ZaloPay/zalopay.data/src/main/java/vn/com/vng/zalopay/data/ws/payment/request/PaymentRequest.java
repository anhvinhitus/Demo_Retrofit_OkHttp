package vn.com.vng.zalopay.data.ws.payment.request;

import android.text.TextUtils;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by hieuvm on 3/8/17.
 */

public class PaymentRequest {

    private static long sPaymentRequestId = 1;

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

    public PaymentRequest(long requestId, String domain, String path, List<Pair<String, String>> params, List<Pair<String, String>> headers, int port, String method, int priority, boolean cancelled) {
        this.priority = priority;
        this.cancelled = cancelled;
        this.requestId = requestId;
        this.params = params;
        this.headers = headers;
        this.path = path;
        this.port = port;
        this.method = method;
        this.domain = domain;
    }

    public static final class Builder {

        public int priority;
        public boolean cancelled;

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

        public PaymentRequest.Builder user(User user) {
            if (this.params == null) {
                this.params = new ArrayList<>();
            }

            Pair<String, String> uid = new Pair<>("userid", user.zaloPayId);
            Pair<String, String> token = new Pair<>("accesstoken", user.accesstoken);
            this.params.add(uid);
            this.params.add(token);
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
                    headers, port, method, priority, cancelled);
        }
    }

}

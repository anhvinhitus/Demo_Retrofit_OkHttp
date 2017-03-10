package vn.com.vng.zalopay.data.paymentconnector;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;
import vn.com.vng.zalopay.data.ws.model.PaymentRequestData;

/**
 * Created by hieuvm on 3/9/17.
 */

public class PaymentConnectorCall implements Call {

    private PaymentConnectorService mClient;
    private boolean executed;
    // private Call rawCall;
    private final Request originalRequest;
    private CountDownLatch doneSignal = new CountDownLatch(1);
    private Response mResponse;

    public PaymentConnectorCall(PaymentConnectorService client, Request request) {
        this.mClient = client;
        this.originalRequest = request;
    }

    @Override
    public Request request() {
        Timber.d("get request");
        return originalRequest;
    }

    @Override
    public Response execute() throws IOException {
        Timber.d("execute");
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already executed");
            executed = true;
        }

        mClient.request(PaymentConnectorFactory.createRequest(originalRequest), new PaymentConnectorCallback() {
            @Override
            public void onResult(PaymentRequestData data) {
                Timber.d("response connector %s", data);
                mResponse = parseResponse(data);
                doneSignal.countDown();
            }

            @Override
            public void onFailure(IOException e) {
                doneSignal.countDown();
            }
        });

        try {
            doneSignal.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            Timber.d(ie);
        }

        return mResponse;
    }


    @Override
    public void enqueue(Callback callback) {
        Timber.d("enqueue: %s", callback);
        if (callback == null) throw new NullPointerException("callback == null");

        synchronized (this) {
            if (executed) throw new IllegalStateException("Already executed");
            executed = true;
        }

        mClient.request(PaymentConnectorFactory.createRequest(originalRequest), new PaymentConnectorCallback() {
            @Override
            public void onResult(PaymentRequestData data) {
                Timber.d("response connector %s", data);
                try {
                    callback.onResponse(PaymentConnectorCall.this, parseResponse(data));
                } catch (IOException e) {
                    callback.onFailure(PaymentConnectorCall.this, e);
                    Timber.d(e, "error ");
                }
            }

            @Override
            public void onFailure(IOException e) {
                try {
                    callback.onFailure(PaymentConnectorCall.this, e);
                } catch (Exception ex) {
                    Timber.d(ex);
                }
            }
        });
    }

    private Response parseResponse(PaymentRequestData data) {
        ResponseBody resp = ResponseBody.create(MediaType.parse("application/json"), data.resultdata);
        return new Response.Builder()
                .body(resp)
                .code(200)
                .protocol(Protocol.HTTP_1_1)
                .request(originalRequest)
                .build();
    }

    @Override
    public void cancel() {
        Timber.d("cancel: ");
    }

    @Override
    public boolean isExecuted() {
        Timber.d("isExecuted: ");
        return false;
    }

    @Override
    public boolean isCanceled() {
        Timber.d("isCanceled: ");
        return false;
    }
}

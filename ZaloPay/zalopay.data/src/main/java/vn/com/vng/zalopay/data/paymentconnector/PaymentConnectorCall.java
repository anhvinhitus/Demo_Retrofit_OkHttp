package vn.com.vng.zalopay.data.paymentconnector;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
 * Implement okhttp3.Call for PaymentConnector
 */

class PaymentConnectorCall implements Call {

    private final PaymentConnectorService mClient;
    private boolean mExecuted;
    private final Request originalRequest;
    private final CountDownLatch doneSignal = new CountDownLatch(1);

    private PaymentRequest mPaymentRequest;
    private boolean isCancelled;
    private long sentRequestAtMillis;
    private long receivedResponseAtMillis;
    private PaymentRequestData mPaymentResponse;

    PaymentConnectorCall(PaymentConnectorService client, @NonNull Request request) {
        this.mClient = client;
        this.originalRequest = request;
    }

    @Override
    public Request request() {
        return originalRequest;
    }

    @Override
    public Response execute() throws IOException {
        synchronized (this) {
            if (mExecuted) {
                throw new IllegalStateException("Already executed");
            }
            mExecuted = true;
        }

        mPaymentResponse = null;
        mPaymentRequest = PaymentConnectorFactory.createRequest(originalRequest);
        mClient.request(mPaymentRequest, new PaymentConnectorCallback() {

            @Override
            public void onStart() {
                Timber.d("Payment request execute url = %s", originalRequest.url().toString());
                sentRequestAtMillis = System.currentTimeMillis();
            }

            @Override
            public void onResponse(@NonNull PaymentRequestData data) {
                receivedResponseAtMillis = System.currentTimeMillis();
                mPaymentResponse = data;
                doneSignal.countDown();
            }

            @Override
            public void onFailure(IOException e) {
                doneSignal.countDown();
            }
        });

        try {
            doneSignal.await(7, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            Timber.d(ie);
        }
        if (mPaymentResponse == null) {
            Timber.d("Payment request timeout requestId [%s]", mPaymentRequest.requestId);
            throw new IOException(new TimeoutException());
        }

        if (isCanceled()) {
            throw new IOException("Cancelled");
        }

        return parseResponse(mPaymentResponse);
    }


    @Override
    public void enqueue(Callback callback) {
        if (callback == null) {
            throw new NullPointerException("callback == null");
        }

        synchronized (this) {
            if (mExecuted) {
                throw new IllegalStateException("Already executed");
            }
            mExecuted = true;
        }

        mPaymentRequest = PaymentConnectorFactory.createRequest(originalRequest);

        mClient.request(mPaymentRequest, new PaymentConnectorCallback() {

            @Override
            public void onStart() {
                Timber.d("Payment request enqueue url = %s", originalRequest.url().toString());
                sentRequestAtMillis = System.currentTimeMillis();
            }

            @Override
            public void onResponse(@NonNull PaymentRequestData data) {
                receivedResponseAtMillis = System.currentTimeMillis();
                try {
                    callback.onResponse(PaymentConnectorCall.this, parseResponse(data));
                } catch (IOException e) {
                    onFailure(e);
                }
            }

            @Override
            public void onFailure(IOException e) {
                callback.onFailure(PaymentConnectorCall.this, e);
            }
        });
    }

    private Response parseResponse(PaymentRequestData data) {
        ResponseBody resp = ResponseBody.create(MediaType.parse("application/json"), data.resultdata);
        return new Response.Builder()
                .body(resp)
                .code(200)
                .sentRequestAtMillis(sentRequestAtMillis)
                .receivedResponseAtMillis(receivedResponseAtMillis)
                .protocol(Protocol.HTTP_1_1)
                .request(originalRequest)
                .build();
    }

    @Override
    public void cancel() {
        isCancelled = true;

        if (mPaymentRequest != null) {
            mPaymentRequest.cancelled = true;
        }

        dispose();

        doneSignal.countDown();
    }

    @Override
    public boolean isExecuted() {
        return mExecuted;
    }

    @Override
    public boolean isCanceled() {
        return isCancelled;
    }

    private void dispose() {
        if (!isExecuted()) {
            return;
        }

        if (mPaymentRequest == null) {
            return;
        }

        mClient.cancel(mPaymentRequest);
    }
}

package vn.com.vng.zalopay.data.paymentconnector;

import android.support.annotation.NonNull;
import android.text.TextUtils;

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
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.R;
import vn.com.vng.zalopay.data.exception.PaymentConnectorException;
import vn.com.vng.zalopay.data.net.adapter.MerchantApiMap;
import vn.com.vng.zalopay.data.protobuf.PaymentCode;
import vn.com.vng.zalopay.data.ws.model.PaymentRequestData;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

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
                Timber.d("payment request start %s", mPaymentRequest.requestId);
                sentRequestAtMillis = System.currentTimeMillis();
            }

            @Override
            public void onResponse(@NonNull PaymentRequestData data) {
                Timber.d("receive response from connector");
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
            doneSignal.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            Timber.d(ie);
        }

        if (mPaymentResponse == null) {
            throw new IOException(new TimeoutException());
        }

        if (isCanceled()) {
            throw new IOException("Cancelled");
        }

        if (mPaymentResponse.resultcode != PaymentCode.PAY_SUCCESS.getValue()) {
            throw new IOException(new PaymentConnectorException(R.string.exception_server_error));
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
                Timber.d("payment request start [%s]", mPaymentRequest.requestId);
                sentRequestAtMillis = System.currentTimeMillis();
            }

            @Override
            public void onResponse(@NonNull PaymentRequestData data) {
                Timber.d("receive response from connector");
                receivedResponseAtMillis = System.currentTimeMillis();
                if (data.resultcode != PaymentCode.PAY_SUCCESS.getValue()) {
                    onFailure(new IOException(new PaymentConnectorException(R.string.exception_server_error)));
                    return;
                }

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
        if (!isExecuted()) {
            return;
        }

        if (mPaymentRequest == null) {
            return;
        }

        mClient.cancel(mPaymentRequest);

    }

    @Override
    public boolean isExecuted() {
        Timber.d("isExecuted: %s", mExecuted);
        return mExecuted;
    }

    @Override
    public boolean isCanceled() {
        Timber.d("isCanceled: %s", isCancelled);
        return isCancelled;
    }
}

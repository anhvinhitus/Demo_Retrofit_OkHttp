package vn.com.vng.zalopay.data.paymentconnector;

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
    private CountDownLatch doneSignal = new CountDownLatch(1);
    private Response mResponse;
    private PaymentRequest mPaymentRequest;
    private boolean isCancelled;

    PaymentConnectorCall(PaymentConnectorService client, Request request) {
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

        mResponse = null;
        mPaymentRequest = PaymentConnectorFactory.createRequest(originalRequest);
        mClient.request(mPaymentRequest, new PaymentConnectorCallback() {
            @Override
            public void onResult(PaymentRequestData data) {
                Timber.d("receive response from connector");
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

        if (mResponse == null) {
            throw new IOException(new TimeoutException());
        }

        return mResponse;
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
            public void onResult(PaymentRequestData data) {
                Timber.d("receive response from connector");
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
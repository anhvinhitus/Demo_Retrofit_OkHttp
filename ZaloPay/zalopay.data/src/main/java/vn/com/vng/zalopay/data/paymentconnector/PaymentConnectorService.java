package vn.com.vng.zalopay.data.paymentconnector;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.LongSparseArray;

import java.util.LinkedList;

import timber.log.Timber;
import vn.com.vng.zalopay.data.ws.callback.OnReceiverMessageListener;
import vn.com.vng.zalopay.data.ws.connection.Connection;
import vn.com.vng.zalopay.data.ws.connection.NotificationApiHelper;
import vn.com.vng.zalopay.data.ws.connection.NotificationApiMessage;
import vn.com.vng.zalopay.data.ws.model.AuthenticationData;
import vn.com.vng.zalopay.data.ws.model.Event;
import vn.com.vng.zalopay.data.ws.model.PaymentRequestData;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by hieuvm on 3/8/17.
 */

public class PaymentConnectorService implements OnReceiverMessageListener {

    private Connection mPaymentService;
    private final LongSparseArray<PaymentConnectorCallback> mPaymentCallBackArray;
    private final LinkedList<PaymentRequest> mRequestQueue;

    public PaymentConnectorService(Connection service) {
        this.mPaymentService = service;
        this.mPaymentCallBackArray = new LongSparseArray<>();
        this.mRequestQueue = new LinkedList<>();
        this.mPaymentService.addReceiverListener(this);
    }

    public void request(@NonNull PaymentRequest request, @NonNull PaymentConnectorCallback callback) {
        mPaymentCallBackArray.put(request.requestId, callback);
        synchronized (mRequestQueue) {
            mRequestQueue.add(request);
        }
        executeNext();
    }

    private NotificationApiMessage transform(PaymentRequest request) {
        return NotificationApiHelper.createPaymentRequestApi(
                request.requestId,
                request.domain,
                request.method,
                request.port,
                request.path,
                request.params,
                request.headers);
    }

    @Nullable
    private PaymentConnectorCallback findCallbackById(long requestId) {
        return mPaymentCallBackArray.get(requestId);
    }

    private void removeCallback(long requestId) {
        synchronized (mPaymentCallBackArray) {
            mPaymentCallBackArray.remove(requestId);
        }
    }

    @Override
    public void onReceiverEvent(Event event) {
        if (event instanceof AuthenticationData) {
            executeNext();
        } else if (event instanceof PaymentRequestData) {
            handleResult((PaymentRequestData) event);
        }
    }

    private void handleResult(PaymentRequestData response) {
        PaymentConnectorCallback callback = findCallbackById(response.requestid);
        if (callback == null) {
            Timber.i("Cannot find callback for request [%s]", response.requestid);
            return;
        }

        Timber.d("Dispatch response for request: %s", response.requestid);
        removeCallback(response.requestid);
        callback.onResult(response);
    }

    private boolean mRunning;

    private void executeNext() {

        Timber.d("executeNext with request queue size: %s", mRequestQueue.size());

        if (mRunning) {
            Timber.d("Skip execute since there is running task");
            return;
        }

        while (!mRequestQueue.isEmpty()) {

            mRunning = true;

            PaymentRequest request = mRequestQueue.peek();
            if (request == null) {
                mRunning = false;
                break;
            }

            if (!mPaymentService.isConnected() || !mPaymentService.isAuthentication()) {
                mRunning = false;
                break;
            }

            if (request.cancelled) {
                mRequestQueue.poll();
                continue;
            }
            Timber.d("about to send request message to server");
            mPaymentService.send(transform(request));
            mRunning = false;
            mRequestQueue.poll();
        }
    }

    public void cancelAll() {
        mRequestQueue.clear();
        mPaymentCallBackArray.clear();
    }

    public void cancel(PaymentRequest request) {
        mPaymentCallBackArray.remove(request.requestId);
        synchronized (mRequestQueue) {
            mRequestQueue.remove(request);
        }
    }
}

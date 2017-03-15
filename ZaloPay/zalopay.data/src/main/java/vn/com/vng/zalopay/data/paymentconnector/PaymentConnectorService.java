package vn.com.vng.zalopay.data.paymentconnector;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.LongSparseArray;

import java.io.IOException;
import java.util.LinkedList;

import timber.log.Timber;
import vn.com.vng.zalopay.data.exception.WriteSocketException;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.data.ws.callback.OnReceiverMessageListener;
import vn.com.vng.zalopay.data.ws.connection.Connection;
import vn.com.vng.zalopay.data.ws.connection.NotificationApiHelper;
import vn.com.vng.zalopay.data.ws.connection.NotificationApiMessage;
import vn.com.vng.zalopay.data.ws.model.AuthenticationData;
import vn.com.vng.zalopay.data.ws.model.Event;
import vn.com.vng.zalopay.data.ws.model.PaymentRequestData;

/**
 * Created by hieuvm on 3/8/17.
 */

public class PaymentConnectorService implements OnReceiverMessageListener {

    private final Connection mPaymentService;
    private final LongSparseArray<PaymentConnectorCallback> mPaymentCallBackArray;
    private final LinkedList<PaymentRequest> mRequestQueue;
    private final Context mContext;

    private long mCurrentRequestId;
    private boolean mRunning;

    public PaymentConnectorService(Context context, Connection service) {
        this.mPaymentCallBackArray = new LongSparseArray<>();
        this.mRequestQueue = new LinkedList<>();
        this.mContext = context;
        this.mPaymentService = service;
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

    private void removeRequest(PaymentRequest request) {
        synchronized (mRequestQueue) {
            mRequestQueue.remove(request);
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

    @Override
    public void onError(Throwable t) {
        Timber.d(t, "receiver error from connector");

        if (!(t instanceof WriteSocketException)) {
            return;
        }

        if (mCurrentRequestId <= 0) {
            return;
        }

        PaymentConnectorCallback callback = findCallbackById(mCurrentRequestId);

        Timber.d("dispatch error for request mCurrentRequestId [%]", mCurrentRequestId);
        if (callback != null) {
            callback.onFailure((WriteSocketException) t);
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

            if (!mPaymentService.isConnected()
                    || !mPaymentService.isAuthentication()) {
                failure(request);
                mRunning = false;
                break;
            }

            if (request.cancelled) {
                mRequestQueue.poll();
                continue;
            }

            mCurrentRequestId = request.requestId;

            Timber.d("about to send request message to server mCurrentRequestId [%s]", mCurrentRequestId);
            boolean result = mPaymentService.send(transform(request));
            mRunning = false;
            mRequestQueue.poll();
        }
    }

    private void failure(PaymentRequest request) {
        if (NetworkHelper.isNetworkAvailable(mContext)) {
            return;
        }

        PaymentConnectorCallback callback = findCallbackById(request.requestId);
        if (callback != null) {
            callback.onFailure(new IOException("No network connect"));
        }
    }

    public void cancelAll() {
        mPaymentService.removeReceiverListener(this);
        synchronized (mRequestQueue) {
            mRequestQueue.clear();
        }
        synchronized (mPaymentCallBackArray) {
            mPaymentCallBackArray.clear();
        }
    }

    public void cancel(PaymentRequest request) {
        removeCallback(request.requestId);
        removeRequest(request);
    }
}

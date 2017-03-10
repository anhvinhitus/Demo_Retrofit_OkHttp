package vn.com.vng.zalopay.data.ws.payment.request;

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

public class PaymentRequestService implements OnReceiverMessageListener {

    private User mUser;
    private Connection mPaymentService;
    private LongSparseArray<PaymentRequestCallback> mPaymentCallBackArray;
    private final LinkedList<PaymentRequest> mRequestQueue;

    public PaymentRequestService(User user, Connection service) {
        this.mUser = user;
        this.mPaymentService = service;
        this.mPaymentCallBackArray = new LongSparseArray<>();
        this.mRequestQueue = new LinkedList<>();
        this.mPaymentService.addReceiverListener(this);
    }

    public void request(@NonNull PaymentRequest request, @Nullable PaymentRequestCallback callback) {
        if (callback != null) {
            mPaymentCallBackArray.put(request.requestId, callback);
        }
        mRequestQueue.add(request);
        executeNext();
    }

    private NotificationApiMessage transform(PaymentRequest request) {
        return NotificationApiHelper.createPaymentRequestApi(request.requestId,
                request.domain,
                request.method, request.port,
                request.path, request.params,
                request.headers);
    }

    @Nullable
    private PaymentRequestCallback findCallbackById(long requestId) {
        return mPaymentCallBackArray.get(requestId);
    }

    private void removeCallback(long requestId) {
        mPaymentCallBackArray.remove(requestId);
    }

    @Override
    public void onReceiverEvent(Event event) {

        Timber.d("onReceiverEvent: [%s]", event.getClass().getSimpleName());

        if (event instanceof AuthenticationData) {
            executeNext();
        } else if (event instanceof PaymentRequestData) {
            handleResult((PaymentRequestData) event);
        }
    }

    private void handleResult(PaymentRequestData response) {
        PaymentRequestCallback callback = findCallbackById(response.requestid);
        Timber.d("handleResult: %s", callback);
        if (callback == null) {
            return;
        }
        callback.handlerResponse(response.resultdata);
        removeCallback(response.requestid);
    }

    private boolean mRunning;

    private void executeNext() {

        Timber.d("executeNext: %s", mRequestQueue.size());

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

            if (!mPaymentService.isConnected()) {
                mRunning = false;
                break;
            }

            if (request.cancelled) {
                mRequestQueue.poll();
                continue;
            }
            Timber.d("send message");
            mPaymentService.send(transform(request));
            mRunning = false;
            mRequestQueue.poll();
        }
    }
}

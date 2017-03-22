package vn.com.vng.zalopay.data.paymentconnector;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.LongSparseArray;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.R;
import vn.com.vng.zalopay.data.exception.PaymentConnectorException;
import vn.com.vng.zalopay.data.exception.WriteSocketException;
import vn.com.vng.zalopay.data.protobuf.PaymentCode;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.data.ws.callback.OnReceiverMessageListener;
import vn.com.vng.zalopay.data.ws.connection.Connection;
import vn.com.vng.zalopay.data.ws.connection.NotificationApiHelper;
import vn.com.vng.zalopay.data.ws.connection.NotificationApiMessage;
import vn.com.vng.zalopay.data.ws.model.AuthenticationData;
import vn.com.vng.zalopay.data.ws.model.Event;
import vn.com.vng.zalopay.data.ws.model.PaymentRequestData;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;

/**
 * Created by hieuvm on 3/8/17.
 * Connector Service
 */

public class PaymentConnectorService implements OnReceiverMessageListener {

    private static final int CONNECT_TIMEOUT = 5; // 5 SECONDS

    private final Connection mPaymentService;
    private final LongSparseArray<PaymentConnectorCallback> mPaymentCallBackArray;
    private final LinkedList<PaymentRequest> mRequestQueue;
    private final Context mContext;

    private long mCurrentRequestId;
    private boolean mRunning;

    private final PublishSubject<Long> publishSubject = PublishSubject.create();
    private final CompositeSubscription mSubscription = new CompositeSubscription();

    public PaymentConnectorService(Context context, Connection service) {
        this.mPaymentCallBackArray = new LongSparseArray<>();
        this.mRequestQueue = new LinkedList<>();
        this.mContext = context;
        this.mPaymentService = service;
        this.mPaymentService.addReceiverListener(this);
    }

    public void request(@NonNull PaymentRequest request, @NonNull PaymentConnectorCallback callback) {
        beginTimer(request);
        mPaymentCallBackArray.put(request.requestId, callback);
        synchronized (mRequestQueue) {
            mRequestQueue.add(request);
        }
        executeNext();
    }

    private void beginTimer(PaymentRequest request) {
        Subscription subscription = publishSubject.subscribeOn(Schedulers.io())
                .filter(requestId -> requestId == request.requestId)
                .take(1)
                //.doOnNext(requestId -> Timber.d("stop requestId: %s", requestId))
                .timeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .subscribe(new DefaultSubscriber<Long>() {
                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof TimeoutException) {
                            failureTimeout(request, e);
                        }
                    }
                });
        mSubscription.add(subscription);
    }

    private void failureTimeout(PaymentRequest request, Throwable e) {
        PaymentConnectorCallback callback = findCallbackById(request.requestId);
        Timber.d("Payment request timeout: path = %s requestId %s", request.path, request.requestId);
        if (callback != null) {
            callback.onFailure(new IOException(e));
        }
        removeRequest(request);
        removeCallback(request.requestId);
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

        publishSubject.onNext(mCurrentRequestId);

        PaymentConnectorCallback callback = findCallbackById(mCurrentRequestId);
        Timber.d("dispatch error for request mCurrentRequestId [%]", mCurrentRequestId);

        if (callback != null) {
            callback.onFailure((WriteSocketException) t);
        }

    }

    private void handleResult(@NonNull PaymentRequestData response) {
        publishSubject.onNext(response.requestid);

        PaymentConnectorCallback callback = findCallbackById(response.requestid);
        if (callback == null) {
            Timber.i("Cannot find callback for request [%s]", response.requestid);
            return;
        }

        if (response.resultcode != PaymentCode.PAY_SUCCESS.getValue()) {
            callback.onFailure(new IOException(new PaymentConnectorException(R.string.exception_server_error)));
        } else {
            callback.onResponse(response);
        }

        Timber.d("Dispatch response for request: %s", response.requestid);
        removeCallback(response.requestid);

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
                boolean failure = failure(request);

                if (failure) {
                    removeCallback(request.requestId);
                    removeRequest(request);
                }

                mRunning = false;
                break;
            }

            if (request.cancelled) {
                removeCallback(request.requestId);
                removeRequest(request);
                publishSubject.onNext(request.requestId);
                mRunning = false;
                continue;
            }

            mCurrentRequestId = request.requestId;

            PaymentConnectorCallback callback = findCallbackById(request.requestId);
            if (callback != null) {
                callback.onStart();
            }

            boolean result = mPaymentService.send(transform(request));
            mRunning = false;
            removeRequest(request);
        }
    }

    private boolean failure(PaymentRequest request) {
        if (NetworkHelper.isNetworkAvailable(mContext)) {
            return false;
        }
        Timber.d("failure request path = %s requestId %s", request.path, request.requestId);

        publishSubject.onNext(request.requestId);
        PaymentConnectorCallback callback = findCallbackById(request.requestId);
        if (callback != null) {
            callback.onFailure(new IOException("No network connect"));
        }

        return true;
    }

    public void cancelAll() {
        mPaymentService.removeReceiverListener(this);
        synchronized (mRequestQueue) {
            mRequestQueue.clear();
        }
        synchronized (mPaymentCallBackArray) {
            mPaymentCallBackArray.clear();
        }
        mSubscription.clear();
    }

    public void cancel(PaymentRequest request) {
        removeCallback(request.requestId);
        removeRequest(request);
        publishSubject.onNext(request.requestId);
    }
}

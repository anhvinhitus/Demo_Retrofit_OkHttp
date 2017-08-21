package vn.com.vng.zalopay.network;

import android.content.Context;
import android.text.TextUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;
import vn.com.vng.zalopay.network.exception.HttpEmptyResponseException;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by huuhoa on 7/4/16.
 * Trigger call on subscribing
 */
public final class CallOnSubscribe<T> implements Observable.OnSubscribe<Response<T>> {
    private final Context mContext;
    private final Call<T> mOriginalCall;
    private final int mHttpsApiId;
    private final int mConnectorApiId;

    public CallOnSubscribe(Context context, Call<T> originalCall, int httpsApiId, int connectorApiId) {
        this.mContext = context;
        this.mOriginalCall = originalCall;
        this.mHttpsApiId = httpsApiId;
        this.mConnectorApiId = connectorApiId;
    }

    @Override
    public void call(final Subscriber<? super Response<T>> subscriber) {
        // Since Call is a one-shot type, clone it for each new subscriber.
        final Call<T> call = mOriginalCall.clone();
        // Attempt to cancel the call if it is still in-flight on unsubscription.
        subscriber.add(Subscriptions.create(call::cancel));

        try {
            long startNs = System.nanoTime();
            Response<T> response = call.execute();
            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            if (response != null && response.isSuccessful()) {
                Timber.d("API request [%s, %s] took %s ms", mHttpsApiId, mConnectorApiId, tookMs);
                logTiming(response.raw().receivedResponseAtMillis() - response.raw().sentRequestAtMillis(), call.request());
            } else {
                errorHandler(call.request(), response, subscriber);
                return;
            }

            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(response);
            }

        } catch (Throwable throwable) {
            Exceptions.throwIfFatal(throwable);
            Throwable t = throwable.getCause() != null ? throwable.getCause() : throwable;

            errorHandler(call.request(), t, subscriber);
            return;
        }

        if (!subscriber.isUnsubscribed()) {
            subscriber.onCompleted();
        }
    }

    private void errorHandler(Request request, Response<T> response, Subscriber<? super Response<T>> subscriber) {
        try {
            errorHandler(request, response, null, subscriber);
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    private void errorHandler(Request request, Throwable t, Subscriber<? super Response<T>> subscriber) {
        try {
            errorHandler(request, null, t, subscriber);
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    /**
     * handler error Response<T>
     */

    private void errorHandler(Request request, Response<T> response, Throwable t, Subscriber<? super Response<T>> subscriber) throws Exception {

        String string = request.toString();
        String accesstoken = request.url().queryParameter("accesstoken");
        if (!TextUtils.isEmpty(accesstoken)) {
            string = string.replace(accesstoken, "[*]");
        }

        if (subscriber.isUnsubscribed()) {
            return;
        }


        int httpCode = 0, networkCode = 0;

        if (t != null) {
            if (!NetworkHelper.isNetworkAvailable(mContext)) {
                subscriber.onError(new NetworkConnectionException());
                networkCode = NetworkErrorCode.NO_NETWORK_CONNECTION;
            } else {
                subscriber.onError(t);
                networkCode = NetworkErrorCode.EMPTY_RESPONSE;
            }

            Timber.w("Error [%s] on request: %s", t.getMessage(), string);
        } else {

            if (response == null) {
                subscriber.onError(new HttpEmptyResponseException());
                networkCode = NetworkErrorCode.EMPTY_RESPONSE;
            } else if (!response.isSuccessful()) {
                subscriber.onError(new HttpException(response));
                httpCode = response.code();
            }

            Timber.w("Error with http_code [%s] on request: %s", response == null ? NetworkErrorCode.NO_NETWORK_CONNECTION : response.code(), string);
        }

        trackingError(request.url().encodedPath().replaceFirst("/", ""), httpCode, 0, networkCode);
    }

    private int getEventId(boolean isPaymentCall) {
        if (isPaymentCall) {
            return mConnectorApiId;
        } else {
            return mHttpsApiId;
        }
    }

    private void trackingError(String apiName, int httpCode, int serverCode, int networkCode) {
        ZPAnalytics.trackAPIError(apiName, httpCode, serverCode, networkCode);
    }

    private void logTiming(long duration, Request request) {

        boolean isPaymentCall = NetworkConstants.CONNECTOR.equals(request.tag());

        int eventId = getEventId(isPaymentCall);
        Timber.d("API Request [%s, %s] (%s), duration: %s (ms)", mHttpsApiId, mConnectorApiId, ZPEvents.actionFromEventId(eventId), duration);
        if (eventId <= 0) {
            eventId = MerchantApiMap.getEventIdByRequest(request);
            if (eventId <= 0) {
                Timber.i("Skip logging timing event");
                return;
            }
        }

        ZPAnalytics.trackTiming(eventId, duration);
    }
}

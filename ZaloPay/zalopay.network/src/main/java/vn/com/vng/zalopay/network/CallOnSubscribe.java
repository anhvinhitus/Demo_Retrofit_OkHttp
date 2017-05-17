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
    private final int mApiClientId;

    public CallOnSubscribe(Context context, Call<T> originalCall, int apiClientId) {
        this.mContext = context;
        this.mOriginalCall = originalCall;
        mApiClientId = apiClientId;
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
                Timber.d("API request %s took %s ms", mApiClientId, tookMs);
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
                networkCode = -1009;
            } else {
                subscriber.onError(t);
                networkCode = -1010;
            }

            Timber.w("Error [%s] on request: %s", t.getMessage(), string);
        } else {

            if (response == null) {
                subscriber.onError(new HttpEmptyResponseException());
                networkCode = -1010;
            } else if (!response.isSuccessful()) {
                subscriber.onError(new HttpException(response));
                httpCode = response.code();
            }

            Timber.w("Error with http_code [%s] on request: %s", response == null ? -1009 : response.code(), string);
        }

        ZPAnalytics.trackAPIError(Utils.pathSegmentsToString(request.url().pathSegments()), httpCode, 0, networkCode);
    }

    private void logTiming(long duration, Request request) {
        Timber.d("API Request %s (%s), duration: %s (ms)", mApiClientId, ZPEvents.actionFromEventId(mApiClientId), duration);
        int eventId = mApiClientId;
        if (eventId <= 0) {
            if (request != null) {
                String path = request.url().encodedPath();
                Timber.d("API Request: %s", path);
                if (MerchantApiMap.gApiMapEvent.containsKey(path)) {
                    Timber.d("Found API Request");
                    eventId = MerchantApiMap.gApiMapEvent.get(path);
                }
            }

            if (eventId <= 0) {
                Timber.i("Skip logging timing event");
                return;
            }
        }

        ZPAnalytics.trackTiming(eventId, duration);
    }
}
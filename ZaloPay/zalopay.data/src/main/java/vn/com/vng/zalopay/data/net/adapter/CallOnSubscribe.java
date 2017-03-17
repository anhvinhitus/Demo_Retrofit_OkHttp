package vn.com.vng.zalopay.data.net.adapter;

import android.content.Context;
import android.text.TextUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;
import vn.com.vng.zalopay.data.exception.NetworkConnectionException;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by huuhoa on 7/4/16.
 * Trigger call on subscribing
 */
final class CallOnSubscribe<T> implements Observable.OnSubscribe<Response<T>> {
    private final Context mContext;
    private final Call<T> mOriginalCall;
    private final int mApiClientId;

    CallOnSubscribe(Context context, Call<T> originalCall, int apiClientId) {
        this.mContext = context;
        this.mOriginalCall = originalCall;
        this.mApiClientId = apiClientId;
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
//                logTiming(endRequestTime - beginRequestTime, call.request());
                Timber.d("API request %s took %s ms", mApiClientId, tookMs);
                okhttp3.Response resp = response.raw();
                logTiming(resp.receivedResponseAtMillis() - resp.sentRequestAtMillis(), call.request());
            }
            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(response);
            }
        } catch (Throwable throwable) {

            Throwable t = throwable.getCause() != null ? throwable.getCause() : throwable;

            Exceptions.throwIfFatal(t);

            if (subscriber.isUnsubscribed()) {
                return;
            }

            // Add tracing for knowing which request results error
            // Remove access token for protecting users

            try {
                String string = call.request().toString();
                String accesstoken = call.request().url().queryParameter("accesstoken");
                if (!TextUtils.isEmpty(accesstoken)) {
                    string = string.replace(accesstoken, "[*]");
                }
                Timber.w("Error [%s] on request: %s", t.getMessage(), string);
            } catch (Throwable tt) {
                // empty
            }

            try {
                if (NetworkHelper.isNetworkAvailable(mContext)) {
                    subscriber.onError(t);
                } else {
                    subscriber.onError(new NetworkConnectionException());
                }
            } catch (Exception ex) {
                Timber.w(ex, "Exception OnError :");
            }
            return;
        }

        if (!subscriber.isUnsubscribed()) {
            subscriber.onCompleted();
        }
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

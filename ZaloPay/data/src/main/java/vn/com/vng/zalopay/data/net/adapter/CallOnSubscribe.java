package vn.com.vng.zalopay.data.net.adapter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.text.TextUtils;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;
import vn.com.vng.zalopay.data.exception.NetworkConnectionException;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.analytics.ZPTracker;

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
        mApiClientId = apiClientId;
    }

    @Override
    public void call(final Subscriber<? super Response<T>> subscriber) {
        // Since Call is a one-shot type, clone it for each new subscriber.
        final Call<T> call = mOriginalCall.clone();

        // Attempt to cancel the call if it is still in-flight on unsubscription.
        subscriber.add(Subscriptions.create(call::cancel));

        try {
            long beginRequestTime = System.currentTimeMillis();
            Response<T> response = call.execute();
            long endRequestTime = System.currentTimeMillis();
            if (response != null && response.isSuccessful()) {
                logTiming(endRequestTime - beginRequestTime);
            }
            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(response);
            }
        } catch (Throwable t) {
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

    private void logTiming(long duration) {
        Timber.i("Request eventId: %s, eventName: %s, duration: %s", mApiClientId, ZPEvents.actionFromEventId(mApiClientId), duration);
        if (mApiClientId <= 0) {
            Timber.i("Skip logging timing event");
            return;
        }

        ZPAnalytics.trackTiming(mApiClientId, duration);
    }
}

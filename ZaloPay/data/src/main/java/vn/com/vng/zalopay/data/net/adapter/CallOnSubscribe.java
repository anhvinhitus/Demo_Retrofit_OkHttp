package vn.com.vng.zalopay.data.net.adapter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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

/**
 * Created by huuhoa on 7/4/16.
 * Trigger call on subscribing
 */
final class CallOnSubscribe<T> implements Observable.OnSubscribe<Response<T>> {
    private final Context mContext;
    private final Call<T> mOriginalCall;

    CallOnSubscribe(Context context, Call<T> originalCall) {
        this.mContext = context;
        this.mOriginalCall = originalCall;
    }

    @Override
    public void call(final Subscriber<? super Response<T>> subscriber) {
        // Since Call is a one-shot type, clone it for each new subscriber.
        final Call<T> call = mOriginalCall.clone();

        // Attempt to cancel the call if it is still in-flight on unsubscription.
        subscriber.add(Subscriptions.create(call::cancel));

        try {
            Response<T> response = call.execute();
            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(response);
            }
        } catch (Throwable t) {
            Exceptions.throwIfFatal(t);
            if (subscriber.isUnsubscribed()) {
                return;
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
}

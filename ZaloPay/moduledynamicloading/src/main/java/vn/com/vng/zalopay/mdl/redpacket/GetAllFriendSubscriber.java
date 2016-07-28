package vn.com.vng.zalopay.mdl.redpacket;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.lang.ref.WeakReference;

import timber.log.Timber;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.mdl.Helpers;
import vn.com.vng.zalopay.mdl.error.PaymentError;

/**
 * Created by longlv on 16/07/2016.
 * Handle get all zalo friends result and translate to WritableArray for consuming in react native
 */
class GetAllFriendSubscriber extends DefaultSubscriber<WritableArray> {

    WeakReference<Promise> promiseWeakReference;

    public GetAllFriendSubscriber(Promise promise) {
        promiseWeakReference = new WeakReference<>(promise);
    }

    @Override
    public void onError(Throwable e) {
        Timber.w(e, "error while getting all friends");
        if (promiseWeakReference == null) {
            return;
        }

        Promise promise = promiseWeakReference.get();
        promise.reject(e);
        promiseWeakReference.clear();
    }

    @Override
    public void onNext(WritableArray writableArray) {
        Timber.d("receive %s friends", writableArray.size());

        if (promiseWeakReference == null) {
            return;
        }

        Promise promise = promiseWeakReference.get();
        Helpers.promiseResolveSuccess(promise, writableArray);
        promiseWeakReference.clear();
    }
}

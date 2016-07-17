package vn.com.vng.zalopay.mdl.internal.subscriber;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;

import java.lang.ref.WeakReference;

import timber.log.Timber;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;

/**
 * Created by longlv on 16/07/2016.
 */
public class OpenPackageSubscriber extends DefaultSubscriber<WritableMap> {

    WeakReference<Promise> promiseWeakReference;

    public OpenPackageSubscriber(Promise promise) {
        promiseWeakReference = new WeakReference<>(promise);
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        Timber.w(e, "error on getting OpenPackageSubscriber");
        if (promiseWeakReference == null) {
            return;
        }

        Promise promise = promiseWeakReference.get();
        promise.reject(e);
        promiseWeakReference.clear();
    }

    @Override
    public void onNext(WritableMap writableMap) {

        Timber.d("OpenPackageSubscriber %s", writableMap);

        if (promiseWeakReference == null) {
            return;
        }

        Promise promise = promiseWeakReference.get();
        promise.resolve(writableMap);
        promiseWeakReference.clear();
    }
}

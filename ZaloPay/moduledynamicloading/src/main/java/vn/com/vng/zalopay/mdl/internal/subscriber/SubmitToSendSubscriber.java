package vn.com.vng.zalopay.mdl.internal.subscriber;

import com.facebook.react.bridge.Promise;

import java.lang.ref.WeakReference;

import timber.log.Timber;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;

/**
 * Created by longlv on 16/07/2016.
 */
public class SubmitToSendSubscriber extends DefaultSubscriber<Boolean> {
    WeakReference<Promise> promiseWeakReference;


    public SubmitToSendSubscriber(Promise promise) {
        promiseWeakReference = new WeakReference<>(promise);
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        Timber.w(e, "error on getting SubmitToSendSubscriber");
        if (promiseWeakReference == null) {
            return;
        }

        Promise promise = promiseWeakReference.get();
        promise.reject(e);
        promiseWeakReference.clear();
    }

    @Override
    public void onNext(Boolean result) {

        Timber.d("SubmitToSendSubscriber %s", result);

        if (promiseWeakReference == null) {
            return;
        }

        Promise promise = promiseWeakReference.get();
        promise.resolve(null);
        promiseWeakReference.clear();
    }
}

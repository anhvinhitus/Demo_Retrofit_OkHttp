package vn.com.vng.zalopay.react.iap;

import com.facebook.react.bridge.Promise;

import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;

import retrofit2.adapter.rxjava.HttpException;
import timber.log.Timber;
import vn.com.vng.zalopay.data.exception.FormatException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;

/**
 * Created by AnhHieu on 9/21/16.
 * *
 */
final class RequestSubscriber extends DefaultSubscriber<String> {

    private WeakReference<Promise> wrPromise;

    RequestSubscriber(Promise promise) {
        wrPromise = new WeakReference<>(promise);
    }

    @Override
    public void onError(Throwable e) {

        Timber.d(e, "request error");
        Promise promise = wrPromise.get();
        if (promise == null) {
            return;
        }

        if (e instanceof FormatException) {
            this.promiseReject(promise, -2000, "Format request error!");
        } else if (e instanceof HttpException) {
            this.promiseReject(promise, ((HttpException) e).code(), "Máy chủ đang bị lỗi. Vui lòng thử lại sau");
        } else if (e instanceof SocketTimeoutException) {
            this.promiseReject(promise, 0, "Quá thời gian kết nối");
        } else {
            this.promiseReject(promise, -1009, "Mạng kết nối không ổn định. Vui lòng kiểm tra kết nối và thử lại");
        }

    }

    private void promiseReject(Promise promise, int errorCode, String message) {
        promise.reject(String.valueOf(errorCode), message);
    }

    @Override
    public void onNext(String s) {
        Timber.d("onNext response %s", s);
        Promise promise = wrPromise.get();
        if (promise == null) {
            return;
        }
        promise.resolve(s);
    }
}

package vn.com.vng.zalopay.react.iap;

import android.text.TextUtils;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;

import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;

import retrofit2.adapter.rxjava.HttpException;
import vn.com.vng.zalopay.data.exception.FormatException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;

/**
 * Created by AnhHieu on 9/21/16.
 * *
 */
final class RequestSubscriber extends DefaultSubscriber<String> {

    WeakReference<Promise> wrPromise;

    public RequestSubscriber(Promise promise) {
        wrPromise = new WeakReference<>(promise);
    }

    @Override
    public void onError(Throwable e) {
        Promise promise = wrPromise.get();
        if (promise == null) {
            return;
        }

        if (e instanceof FormatException) {
            this.promiseResolveError(promise, -2000, "Format request error!");
        } else if (e instanceof HttpException) {
            this.promiseResolveError(promise, ((HttpException) e).code(), "Máy chủ đang bị lỗi. Vui lòng thử lại sau");
        } else if (e instanceof SocketTimeoutException) {
            this.promiseResolveError(promise, 0, "Quá thời gian kết nối");
        } else {
            this.promiseResolveError(promise, -1009, "Mạng kết nối không ổn định. Vui lòng kiểm tra kết nối và thử lại");
        }

    }

    private void promiseResolveError(Promise promise, int errorCode, String message) {
        WritableMap item = Arguments.createMap();
        item.putInt("error_code", errorCode);
        if (!TextUtils.isEmpty(message)) {
            item.putString("error_message", message);
        }
        promise.resolve(item);
    }

    @Override
    public void onNext(String s) {
        Promise promise = wrPromise.get();
        if (promise == null) {
            return;
        }
        promise.resolve(s);
    }
}

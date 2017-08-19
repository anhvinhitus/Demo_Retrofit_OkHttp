package vn.com.vng.zalopay.react.iap;

import com.facebook.react.bridge.Promise;

import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;

import retrofit2.adapter.rxjava.HttpException;
import vn.com.vng.zalopay.data.exception.FormatException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;

/**
 * Created by AnhHieu on 9/21/16.
 * *
 */
public final class RequestSubscriber extends DefaultSubscriber<String> {

    private Promise mPromise;

    public RequestSubscriber(Promise promise) {
        mPromise = promise;
    }

    @Override
    public void onError(Throwable e) {
        if (mPromise == null) {
            return;
        }

        if (e instanceof FormatException) {
            this.promiseReject(mPromise, -2000, "Format request error!");
        } else if (e instanceof HttpException) {
            this.promiseReject(mPromise, ((HttpException) e).code(), "Máy chủ đang bị lỗi. Vui lòng thử lại sau");
        } else if (e instanceof SocketTimeoutException) {
            this.promiseReject(mPromise, 0, "Quá thời gian kết nối");
        } else {
            this.promiseReject(mPromise, -1009, "Mạng kết nối không ổn định. Vui lòng kiểm tra kết nối và thử lại");
        }

        mPromise = null;
    }

    private void promiseReject(Promise promise, int errorCode, String message) {
        promise.reject(String.valueOf(errorCode), message);
    }

    @Override
    public void onNext(String s) {
        if (mPromise == null) {
            return;
        }
        mPromise.resolve(s);

        mPromise = null;
    }
}

package vn.com.vng.zalopay.mdl.redpacket;

import android.text.TextUtils;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;

import java.util.Arrays;

import timber.log.Timber;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.exception.NetworkConnectionException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.mdl.error.PaymentError;

/**
 * Created by longlv on 28/07/2016.
 * define an error-handler for redpacket requests
 */
public class RedPacketSubscriber<T> extends DefaultSubscriber<T> {
    private final Promise mPromise;

    public RedPacketSubscriber(Promise promise) {
        mPromise = promise;
    }

    @Override
    public void onCompleted() {
        // no-op by default.
    }

    @Override
    public void onError(Throwable e) {
        if (e instanceof BodyException) {
            errorCallback(mPromise, ((BodyException) e).errorCode, ((BodyException) e).message);
        } else if (e instanceof NetworkConnectionException) {
            errorCallback(mPromise, PaymentError.ERR_CODE_INTERNET,
                    PaymentError.getErrorMessage(PaymentError.ERR_CODE_INTERNET));
        } else {
            mPromise.reject("EXCEPTION", e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    public void onNext(T t) {
        // no-op by default.
    }

    private void errorCallback(Promise promise, int errorCode, String message) {
        Timber.d("errorCallback start errorCode [%s] message [%s]", errorCode, message);
        if (promise == null) {
            return;
        }
        WritableMap item = Arguments.createMap();
        item.putInt("code", errorCode);
        if (!TextUtils.isEmpty(message)) {
            item.putString("message", message);
        }
        promise.resolve(item);
    }
}

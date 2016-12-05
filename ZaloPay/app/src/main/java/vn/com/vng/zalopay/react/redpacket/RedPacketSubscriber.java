package vn.com.vng.zalopay.react.redpacket;

import com.facebook.react.bridge.Promise;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

import timber.log.Timber;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.exception.NetworkConnectionException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.react.Helpers;
import vn.com.vng.zalopay.react.error.PaymentError;

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
    public void onError(Throwable e) {
        Timber.d("onError exception [%s]", e);
        if (mPromise == null) {
            return;
        }

        if (e instanceof BodyException) {
            Helpers.promiseResolveError(mPromise, ((BodyException) e).errorCode, ((BodyException) e).message);
        } else if (e instanceof NetworkConnectionException ||
                e instanceof SocketTimeoutException ||
                e instanceof UnknownHostException ||
                e instanceof SSLHandshakeException ||
                e instanceof SSLPeerUnverifiedException ) {
            Helpers.promiseResolveError(mPromise, PaymentError.ERR_CODE_INTERNET.value(),
                    PaymentError.getErrorMessage(PaymentError.ERR_CODE_INTERNET));
        } else {
            Timber.w("EXCEPTION [%s] stackTrace [%s]", e.getMessage(), Arrays.toString(e.getStackTrace()));
            Helpers.promiseResolveError(mPromise, PaymentError.ERR_CODE_SYSTEM.value(), "Có lỗi xảy ra trong quá trình xử lý. Vui lòng thử lại sau.");
        }
    }
}

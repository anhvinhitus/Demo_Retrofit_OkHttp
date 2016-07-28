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
import vn.com.vng.zalopay.mdl.Helpers;
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
    public void onError(Throwable e) {
        if (mPromise == null) {
            return;
        }

        if (e instanceof BodyException) {
            Helpers.promiseResolveError(mPromise, ((BodyException) e).errorCode, ((BodyException) e).message);
        } else if (e instanceof NetworkConnectionException) {
            Helpers.promiseResolveError(mPromise, PaymentError.ERR_CODE_INTERNET,
                    PaymentError.getErrorMessage(PaymentError.ERR_CODE_INTERNET));
        } else {
            mPromise.reject("EXCEPTION", e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }
}

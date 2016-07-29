package vn.com.vng.zalopay.service;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;

import timber.log.Timber;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.MerchantUserInfo;
import vn.com.vng.zalopay.mdl.Helpers;
import vn.com.vng.zalopay.mdl.error.PaymentError;

/**
 * Created by huuhoa on 7/29/16.
 * Process request for merchant user info
 */
final class MerchantUserInfoSubscriber extends DefaultSubscriber<MerchantUserInfo> {
    private final Promise mPromise;

    public MerchantUserInfoSubscriber(Promise promise) {
        this.mPromise = promise;
    }

    @Override
    public void onError(Throwable e) {
        if (ResponseHelper.shouldIgnoreError(e)) {
            // simply ignore the error
            // because it is handled from based activity
            return;
        }

        Timber.w(e, "Error on getting merchant user information");

        Helpers.promiseResolveError(mPromise, getErrorCode(e), null);
    }

    @Override
    public void onNext(MerchantUserInfo merChantUserInfo) {
        Timber.d("get merchant user info %s", merChantUserInfo.muid);
        Helpers.promiseResolveSuccess(mPromise, transform(merChantUserInfo));
    }

    private WritableMap transform(MerchantUserInfo merChantUserInfo) {
        if (merChantUserInfo == null) {
            return null;
        }

        WritableMap data = Arguments.createMap();
        data.putString("mUid", merChantUserInfo.muid);
        data.putString("mAccessToken", merChantUserInfo.maccesstoken);
        data.putString("displayName", merChantUserInfo.displayname);
        data.putString("dateOfBirth", merChantUserInfo.birthdate);
        data.putString("gender", String.valueOf(merChantUserInfo.usergender));
        return data;
    }

    private int getErrorCode(Throwable e) {
        if (e instanceof BodyException) {
            return ((BodyException) e).errorCode;
        } else {
            return PaymentError.ERR_CODE_UNKNOWN;
        }
    }
}

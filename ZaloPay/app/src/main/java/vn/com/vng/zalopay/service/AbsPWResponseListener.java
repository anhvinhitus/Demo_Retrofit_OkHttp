package vn.com.vng.zalopay.service;

import android.app.Activity;

import java.lang.ref.WeakReference;

import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.exception.PaymentWrapperException;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;

/**
 * Created by hieuvm on 12/1/16.
 */

public abstract class AbsPWResponseListener implements PaymentWrapper.IResponseListener {

    public abstract void onError(PaymentWrapperException exception);

    public abstract void onCompleted();

    private WeakReference<Activity> mAct;
    private Navigator mNavigator;

    public AbsPWResponseListener(Activity activity) {
        this(new WeakReference<>(activity));
    }

    public AbsPWResponseListener(WeakReference<Activity> activity) {
        mAct = activity;
        mNavigator = AndroidApplication.instance().getAppComponent().navigator();
    }

    @Override
    public void onParameterError(String param) {
        Activity activity = mAct.get();
        if (activity == null) {
            return;
        }

        if ("order".equalsIgnoreCase(param)) {
            this.onError(new PaymentWrapperException(activity.getString(R.string.order_invalid)));
        } else if ("uid".equalsIgnoreCase(param)) {
            this.onError(new PaymentWrapperException(activity.getString(R.string.user_invalid)));
        } else if ("token".equalsIgnoreCase(param)) {
            this.onError(new PaymentWrapperException(activity.getString(R.string.order_invalid)));
        }

    }

    @Override
    public void onResponseError(PaymentError status) {
        Activity activity = mAct.get();
        if (activity == null) {
            return;
        }

        if (status == PaymentError.ERR_CODE_INTERNET) {
            this.onError(new PaymentWrapperException(status.value(), activity.getString(R.string.exception_no_connection_try_again)));
        } else {
            this.onError(new PaymentWrapperException(status.value(), PaymentError.getErrorMessage(status)));
        }
    }

    @Override
    public void onResponseTokenInvalid() {
        Activity activity = mAct.get();
        if (activity == null) {
            return;
        }

        this.onError(new PaymentWrapperException(NetworkError.TOKEN_INVALID, activity.getString(R.string.exception_token_expired_message)));
    }

    @Override
    public void onAppError(String msg) {
        Activity activity = mAct.get();
        if (activity == null) {
            return;
        }

        this.onError(new PaymentWrapperException(activity.getString(R.string.exception_generic)));

    }

    @Override
    public void onPreComplete(boolean isSuccessful, String pTransId, String pAppTransId) {
    }

    @Override
    public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
        this.onCompleted();
    }
}

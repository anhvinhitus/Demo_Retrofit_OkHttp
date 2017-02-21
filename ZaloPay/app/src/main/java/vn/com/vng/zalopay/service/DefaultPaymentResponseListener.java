package vn.com.vng.zalopay.service;

import android.text.TextUtils;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.event.TokenPaymentExpiredEvent;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;

/**
 * Created by huuhoa on 12/8/16.
 * Default response handler
 */

public abstract class DefaultPaymentResponseListener implements PaymentWrapper.IResponseListener {
    protected abstract ILoadDataView getView();

    public DefaultPaymentResponseListener() {
    }

    @Override
    public void onParameterError(String param) {
        Timber.d("SDK Response Parameter Error: [%s]", param);
        ILoadDataView view = getView();
        if (view == null) {
            return;
        }

        if ("order".equalsIgnoreCase(param)) {
            view.showError(view.getContext().getString(R.string.order_invalid));
        } else if ("uid".equalsIgnoreCase(param)) {
            view.showError(view.getContext().getString(R.string.user_invalid));
        } else if ("token".equalsIgnoreCase(param)) {
            view.showError(view.getContext().getString(R.string.order_invalid));
        } else if (!TextUtils.isEmpty(param)) {
            view.showError(param);
        }
        view.hideLoading();
    }

    @Override
    public void onResponseError(PaymentError status) {
        Timber.d("SDK Response Error: [%s]", status);

        ILoadDataView view = getView();
        if (view == null) {
            return;
        }

        if (status == PaymentError.ERR_CODE_INTERNET) {
            view.hideLoading();
            view.showNetworkErrorDialog();
        }
    }

    @Override
    public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {

    }

    @Override
    public void onResponseTokenInvalid() {
        Timber.d("onResponseTokenInvalid - cleanup and logout");
        if (getView() == null) {
            return;
        }

        ApplicationComponent applicationComponent = AndroidApplication.instance().getAppComponent();
        applicationComponent.eventBus().postSticky(new TokenPaymentExpiredEvent());
    }

    @Override
    public void onAppError(String msg) {
        Timber.d("SDK Response App Error: [%s]", msg);

        ILoadDataView view = getView();
        if (view == null) {
            return;
        }

        String message = msg;
        if (TextUtils.isEmpty(msg) && view.getContext() != null) {
            message = view.getContext().getString(R.string.exception_generic);
        }
        view.showError(message);
        view.hideLoading();
    }

    @Override
    public void onPreComplete(boolean isSuccessful, String pTransId, String pAppTransId) {

    }

}

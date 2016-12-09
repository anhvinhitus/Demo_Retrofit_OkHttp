package vn.com.vng.zalopay.service;

import android.text.TextUtils;

import java.lang.ref.WeakReference;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.repository.ApplicationSession;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;

/**
 * Created by huuhoa on 12/8/16.
 * Default response handler
 */

public class DefaultPaymentResponseListener implements PaymentWrapper.IResponseListener {
    private final WeakReference<ILoadDataView> mDataView;

    public DefaultPaymentResponseListener(ILoadDataView dataView) {
        mDataView = new WeakReference<>(dataView);
    }

    @Override
    public void onParameterError(String param) {
        Timber.d("SDK Response Parameter Error: [%s]", param);

        if (mDataView.get() == null) {
            return;
        }

        ILoadDataView view = mDataView.get();
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

        if (mDataView.get() == null) {
            return;
        }

        ILoadDataView view = mDataView.get();
        if (status == PaymentError.ERR_CODE_INTERNET) {
            view.showNetworkErrorDialog();
        }
    }

    @Override
    public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {

    }

    @Override
    public void onResponseTokenInvalid() {
        Timber.d("onResponseTokenInvalid - cleanup and logout");

        ApplicationSession applicationSession = AndroidApplication.instance().getAppComponent().applicationSession();
        applicationSession.setMessageAtLogin(R.string.exception_token_expired_message);
        applicationSession.clearUserSession();
    }

    @Override
    public void onAppError(String msg) {
        Timber.d("SDK Response App Error: [%s]", msg);

        if (mDataView.get() == null) {
            return;
        }

        ILoadDataView view = mDataView.get();
        String message = msg;
        if (TextUtils.isEmpty(msg) && view.getContext() != null) {
            message = view.getContext().getString(R.string.exception_generic);
        }
        view.showError(message);
        view.hideLoading();
    }

    @Override
    public void onNotEnoughMoney() {

    }

    @Override
    public void onPreComplete(boolean isSuccessful, String pTransId, String pAppTransId) {

    }
}

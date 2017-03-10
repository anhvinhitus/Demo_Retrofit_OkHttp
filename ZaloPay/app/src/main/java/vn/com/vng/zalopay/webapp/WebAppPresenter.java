package vn.com.vng.zalopay.webapp;

import org.json.JSONObject;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.ui.presenter.AbstractPaymentPresenter;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;

/**
 * Created by longlv on 2/9/17.
 * *
 */

class WebAppPresenter extends AbstractPaymentPresenter<IWebAppView> {
    private IPaymentListener mResponseListener;

    @Inject
    WebAppPresenter(BalanceStore.Repository balanceRepository,
                    ZaloPayRepository zaloPayRepository,
                    TransactionStore.Repository transactionRepository,
                    Navigator navigator) {
        super(balanceRepository, zaloPayRepository, transactionRepository, navigator);
    }


    public void pay(JSONObject data, IPaymentListener listener) {
        if (data == null) {
            Timber.i("Pay fail because json is null.");
            return;
        }
        mResponseListener = listener;
        Timber.d("start to process paying order: %s", data.toString());
        if (!NetworkHelper.isNetworkAvailable(mView.getContext())) {
            listener.onPayError(3, PaymentError.getErrorMessage(PaymentError.ERR_CODE_INTERNET));
            return;
        }

        try {
            showLoadingView();
            if (zpTransaction(data)) {
                hideLoadingView();
                return;
            }

            if (orderTransaction(data)) {
                hideLoadingView();
                return;
            }

            hideLoadingView();
            listener.onPayError(2, PaymentError.getErrorMessage(PaymentError.ERR_CODE_INPUT));
        } catch (IllegalArgumentException e) {
            Timber.i("Invalid JSON input: %s", e.getMessage());
        }
    }

    private void showLoadingView() {
        if (mView != null) {
            mView.showLoading();
        }
    }

    private void hideLoadingView() {
        if (mView != null) {
            mView.hideLoading();
        }
    }

    @Override
    public void onPayParameterError(String param) {
        if (mResponseListener != null) {
            mResponseListener.onPayError(param);
        }
    }

    @Override
    public void onPayResponseError(PaymentError paymentError) {
        if (mResponseListener != null) {
            if(paymentError == PaymentError.ERR_CODE_USER_CANCEL) {
                mResponseListener.onPayError(4, PaymentError.getErrorMessage(paymentError));
            } else {
                mResponseListener.onPayError(PaymentError.getErrorMessage(paymentError));
            }
        }
    }

    @Override
    public void onPayResponseSuccess(ZPPaymentResult zpPaymentResult) {
        if (mResponseListener != null) {
            mResponseListener.onPaySuccess();
        }
    }

    @Override
    public void onPayAppError(String msg) {
        if (mResponseListener != null) {
            mResponseListener.onPayError(msg);
        }
    }

}

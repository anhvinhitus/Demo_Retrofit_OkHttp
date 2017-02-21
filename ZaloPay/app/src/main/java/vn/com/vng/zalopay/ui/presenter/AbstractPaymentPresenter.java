package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import org.json.JSONObject;

import timber.log.Timber;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.ZPTransaction;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.DefaultPaymentRedirectListener;
import vn.com.vng.zalopay.service.DefaultPaymentResponseListener;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.service.PaymentWrapperBuilder;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;

/**
 * Created by longlv on 2/10/17.
 * *
 */
public abstract class AbstractPaymentPresenter<View extends IPaymentDataView> extends AbstractPresenter<View> {
    protected Navigator mNavigator;
    protected PaymentWrapper paymentWrapper;

    public abstract void onPayParameterError(String param);

    public abstract void onPayResponseError(PaymentError paymentError);

    public abstract void onPayResponseSuccess(ZPPaymentResult zpPaymentResult);

    public abstract void onPayAppError(String msg);

    public AbstractPaymentPresenter(BalanceStore.Repository balanceRepository,
                                    ZaloPayRepository zaloPayRepository,
                                    TransactionStore.Repository transactionRepository,
                                    Navigator navigator) {
        mNavigator = navigator;
        paymentWrapper = new PaymentWrapperBuilder()
                .setBalanceRepository(balanceRepository)
                .setZaloPayRepository(zaloPayRepository)
                .setTransactionRepository(transactionRepository)
                .setResponseListener(new PaymentResponseListener())
                .setRedirectListener(new DefaultPaymentRedirectListener(mNavigator) {
                    @Override
                    public Object getContext() {
                        return getFragment();
                    }
                })
                .build();
    }

    protected boolean zpTransaction(JSONObject jsonObject) {

        ZPTransaction zpTransaction = new ZPTransaction(jsonObject);
        boolean isValidZPTransaction = zpTransaction.isValid();

        Timber.d("Trying with zptranstoken [%s] activity [%s]", isValidZPTransaction, getActivity());
        if (isValidZPTransaction) {
            paymentWrapper.payWithToken(getActivity(), zpTransaction.appId, zpTransaction.transactionToken);
        }
        return isValidZPTransaction;
    }

    protected boolean orderTransaction(JSONObject jsonOrder) {
        Order order = new Order(jsonOrder);
        boolean isValidOrder = order.isValid();
        if (isValidOrder) {
            paymentWrapper.payWithOrder(getActivity(), order);
        }
        return isValidOrder;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (paymentWrapper == null) {
            return;
        }
        paymentWrapper.onActivityResult(requestCode, resultCode, data);
    }

    private class PaymentResponseListener extends DefaultPaymentResponseListener {

        @Override
        protected ILoadDataView getView() {
            return mView;
        }

        @Override
        public void onParameterError(String param) {
            super.onParameterError(param);
            onPayParameterError(param);
        }

        @Override
        public void onResponseError(PaymentError paymentError) {
            super.onResponseError(paymentError);
            onPayResponseError(paymentError);
        }

        @Override
        public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
            super.onResponseSuccess(zpPaymentResult);
            onPayResponseSuccess(zpPaymentResult);
        }

        @Override
        public void onAppError(String msg) {
            super.onAppError(msg);
            onPayAppError(msg);
        }
    }

    public Activity getActivity() {
        if (mView != null) {
            return mView.getActivity();
        } else {
            return null;
        }
    }

    public Fragment getFragment() {
        if (mView != null) {
            return mView.getFragment();
        } else {
            return null;
        }
    }

}

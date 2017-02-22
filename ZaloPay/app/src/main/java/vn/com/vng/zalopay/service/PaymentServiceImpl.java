package vn.com.vng.zalopay.service;

import android.app.Activity;
import android.content.Intent;

import com.facebook.react.bridge.Promise;

import java.util.Locale;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.merchant.MerchantStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.Helpers;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.react.iap.IPaymentService;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;

/**
 * Created by longlv on 02/06/2016.
 * Implement IPaymentService
 */
public class PaymentServiceImpl implements IPaymentService {

    private final MerchantStore.Repository mMerchantRepository;
    private final BalanceStore.Repository mBalanceRepository;
    private final TransactionStore.Repository mTransactionRepository;
    private PaymentWrapper mPaymentWrapper;
    protected final Navigator navigator = AndroidApplication.instance().getAppComponent().navigator();

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public PaymentServiceImpl(MerchantStore.Repository zaloPayIAPRepository,
                              BalanceStore.Repository balanceRepository,
                              TransactionStore.Repository transactionRepository) {
        this.mMerchantRepository = zaloPayIAPRepository;
        this.mBalanceRepository = balanceRepository;
        this.mTransactionRepository = transactionRepository;
    }

    @Override
    public void pay(Activity activity, final Promise promise, Order order) {

        this.mPaymentWrapper = new PaymentWrapperBuilder()
                .setBalanceRepository(mBalanceRepository)
                .setZaloPayRepository(null)
                .setTransactionRepository(mTransactionRepository)
                .setResponseListener(new PaymentResponseListener(promise))
                .build();

        this.mPaymentWrapper.payWithOrder(activity, order);
    }

    private void unsubscribeIfNotNull(CompositeSubscription subscription) {
        if (subscription != null) {
            subscription.clear();
        }
    }

    private void reportInvalidParameter(Promise promise, String parameterName) {
        if (promise == null) {
            return;
        }

        String message = String.format(Locale.getDefault(), "invalid %s", parameterName);
        Timber.d("Invalid parameter [%s]", parameterName);
        Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_INPUT.value(), message);
    }

    @Override
    public void getUserInfo(Promise promise, long appId) {
        Timber.d("get user info appId %s", appId);
        Subscription subscription = mMerchantRepository.getMerchantUserInfo(appId)
                .subscribe(new MerchantUserInfoSubscriber(promise));
        compositeSubscription.add(subscription);
    }

    public void destroyVariable() {
//        paymentListener = null;
        mPaymentWrapper = null;
        unsubscribeIfNotNull(compositeSubscription);
    }


    @Override
    public void shareMessageToOtherApp(Activity activity, String message) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
        try {
            activity.startActivity(Intent.createChooser(sharingIntent, "Share via"));
        } catch (Exception e) {
            //empty
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mPaymentWrapper != null) {
            mPaymentWrapper.onActivityResult(requestCode, resultCode, data);
        }
    }

    private class PaymentResponseListener extends DefaultPaymentResponseListener {
        private final Promise mPromise;

        public PaymentResponseListener(Promise promise) {
            mPromise = promise;
        }

        @Override
        protected ILoadDataView getView() {
            return null;
        }

        @Override
        public void onParameterError(String param) {
            reportInvalidParameter(mPromise, param);
        }

        @Override
        public void onResponseError(PaymentError paymentError) {
            Helpers.promiseResolveError(mPromise, paymentError.value(),
                    PaymentError.getErrorMessage(paymentError));
        }

        @Override
        public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
            Helpers.promiseResolveSuccess(mPromise, null);
        }

        @Override
        public void onAppError(String msg) {
            Helpers.promiseResolveError(mPromise, PaymentError.ERR_CODE_SYSTEM.value(),
                    PaymentError.getErrorMessage(PaymentError.ERR_CODE_SYSTEM));
            destroyVariable();
        }
    }
}

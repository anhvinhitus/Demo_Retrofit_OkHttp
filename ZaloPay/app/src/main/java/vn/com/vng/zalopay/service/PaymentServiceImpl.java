package vn.com.vng.zalopay.service;

import android.app.Activity;

import com.facebook.react.bridge.Promise;

import java.lang.ref.WeakReference;
import java.util.Locale;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayIAPRepository;
import vn.com.vng.zalopay.react.Helpers;
import vn.com.vng.zalopay.react.iap.IPaymentService;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;

/**
 * Created by longlv on 02/06/2016.
 * Implement IPaymentService
 */
public class PaymentServiceImpl implements IPaymentService {

    final ZaloPayIAPRepository zaloPayIAPRepository;
    final BalanceStore.Repository mBalanceRepository;
    final User user;
    final TransactionStore.Repository mTransactionRepository;
    private PaymentWrapper mPaymentWrapper;
    protected final Navigator navigator = AndroidApplication.instance().getAppComponent().navigator();

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public PaymentServiceImpl(ZaloPayIAPRepository zaloPayIAPRepository, BalanceStore.Repository balanceRepository, User user, TransactionStore.Repository transactionRepository) {
        this.zaloPayIAPRepository = zaloPayIAPRepository;
        this.mBalanceRepository = balanceRepository;
        this.user = user;
        mTransactionRepository = transactionRepository;
    }

    @Override
    public void pay(Activity activity, final Promise promise, Order order) {

        final WeakReference<Activity> mWeakReference = new WeakReference<>(activity);

        this.mPaymentWrapper = new PaymentWrapper(mBalanceRepository, null, mTransactionRepository, new PaymentWrapper.IViewListener() {
            @Override
            public Activity getActivity() {
                return mWeakReference.get();
            }
        }, new PaymentWrapper.IResponseListener() {
            @Override
            public void onParameterError(String param) {
                reportInvalidParameter(promise, param);
            }

            @Override
            public void onResponseError(PaymentError paymentError) {
                Helpers.promiseResolveError(promise, paymentError.value(),
                        PaymentError.getErrorMessage(paymentError));
            }

            @Override
            public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                Helpers.promiseResolveSuccess(promise, null);
            }

            @Override
            public void onResponseTokenInvalid() {
                Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_TOKEN_INVALID.value(),
                        PaymentError.getErrorMessage(PaymentError.ERR_CODE_TOKEN_INVALID));
            }

            @Override
            public void onResponseCancel() {
                Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_USER_CANCEL.value(),
                        PaymentError.getErrorMessage(PaymentError.ERR_CODE_USER_CANCEL));
                destroyVariable();
            }

            @Override
            public void onNotEnoughMoney() {
                navigator.startDepositActivity(AndroidApplication.instance().getApplicationContext());
            }
        });

        this.mPaymentWrapper.payWithOrder(order);
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

        Subscription subscription = zaloPayIAPRepository.getMerchantUserInfo(appId)
                .subscribe(new MerchantUserInfoSubscriber(promise));
        compositeSubscription.add(subscription);
    }

    public void destroyVariable() {
//        paymentListener = null;
        mPaymentWrapper = null;
        unsubscribeIfNotNull(compositeSubscription);
    }

}

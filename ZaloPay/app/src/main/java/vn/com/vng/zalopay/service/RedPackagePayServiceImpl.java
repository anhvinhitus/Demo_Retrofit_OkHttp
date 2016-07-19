package vn.com.vng.zalopay.service;

import android.app.Activity;

import java.lang.ref.WeakReference;

import rx.subscriptions.CompositeSubscription;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.model.BundleOrder;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayIAPRepository;
import vn.com.vng.zalopay.mdl.redpackage.IRedPackagePayListener;
import vn.com.vng.zalopay.mdl.redpackage.IRedPackagePayService;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;

/**
 * Created by longlv on 19/07/2016.
 * This is payment service that RedPackage module used to pay by zaloPaymentSDK
 */
public class RedPackagePayServiceImpl implements IRedPackagePayService {

    final ZaloPayIAPRepository zaloPayIAPRepository;
    final BalanceStore.Repository mBalanceRepository;
    final User user;
    final TransactionStore.Repository mTransactionRepository;
    private PaymentWrapper paymentWrapper;
    protected final Navigator navigator = AndroidApplication.instance().getAppComponent().navigator();

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public RedPackagePayServiceImpl(ZaloPayIAPRepository zaloPayIAPRepository, BalanceStore.Repository balanceRepository, User user, TransactionStore.Repository transactionRepository) {
        this.zaloPayIAPRepository = zaloPayIAPRepository;
        this.mBalanceRepository = balanceRepository;
        this.user = user;
        this.mTransactionRepository = transactionRepository;
    }

    @Override
    public void pay(Activity activity, BundleOrder bundleOrder, final IRedPackagePayListener listener) {

        final WeakReference<Activity> mWeakReference = new WeakReference<>(activity);

        this.paymentWrapper = new PaymentWrapper(mBalanceRepository, null, new PaymentWrapper.IViewListener() {
            @Override
            public Activity getActivity() {
                return mWeakReference.get();
            }
        }, new PaymentWrapper.IResponseListener() {
            @Override
            public void onParameterError(String param) {
                if (listener!= null) {
                    listener.onParameterError(param);
                }
            }

            @Override
            public void onResponseError(int status) {
                if (listener!= null) {
                    listener.onResponseError(status);
                }
            }

            @Override
            public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                updateTransaction();
                balanceUpdate();
                if (listener!= null) {
                    listener.onResponseSuccess(null);
                }
            }

            @Override
            public void onResponseTokenInvalid() {

            }

            @Override
            public void onResponseCancel() {
                if (listener != null) {
                    listener.onResponseCancel();
                }
                destroyVariable();
            }

            @Override
            public void onNotEnoughMoney() {
                navigator.startDepositActivity(AndroidApplication.instance().getApplicationContext());
            }
        });

        this.paymentWrapper.payWithOrder(bundleOrder);
    }

    private void unsubscribeIfNotNull(CompositeSubscription subscription) {
        if (subscription != null) {
            subscription.clear();
        }
    }

    public void destroyVariable() {
//        paymentListener = null;
        paymentWrapper = null;
        unsubscribeIfNotNull(compositeSubscription);
    }

    private void updateTransaction() {
        mTransactionRepository.updateTransaction();
    }

    private void balanceUpdate() {
        // update balance
        mBalanceRepository.updateBalance();
    }
}

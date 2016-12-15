package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.app.ApplicationState;
import vn.com.vng.zalopay.balancetopup.ui.view.IBalanceTopupView;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.exception.NetworkConnectionException;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ApplicationSession;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.service.DefaultPaymentResponseListener;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.service.PaymentWrapperBuilder;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.enumeration.ETransactionType;

/**
 * Created by longlv on 10/05/2016.
 * *
 */
public class BalanceTopupPresenter extends AbstractPresenter<IBalanceTopupView> {

    private final ZaloPayRepository mZaloPayRepository;
    private final PaymentWrapper paymentWrapper;
    private final ApplicationSession mApplicationSession;

    private Navigator mNavigator;
    private User mUser;

    @Inject
    BalanceTopupPresenter(BalanceStore.Repository balanceRepository,
                          ZaloPayRepository zaloPayRepository,
                          TransactionStore.Repository transactionRepository,
                          ApplicationSession applicationSession,
                          User user,
                          Navigator navigator) {
        mZaloPayRepository = zaloPayRepository;
        mApplicationSession = applicationSession;
        mUser = user;
        mNavigator = navigator;
        paymentWrapper = new PaymentWrapperBuilder()
                .setBalanceRepository(balanceRepository)
                .setZaloPayRepository(zaloPayRepository)
                .setTransactionRepository(transactionRepository)
                .setResponseListener(new PaymentResponseListener())
                .setRedirectListener(new PaymentRedirectListener())
                .build();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (paymentWrapper == null) {
            return;
        }

        paymentWrapper.onActivityResult(requestCode, resultCode, data);
    }

    private void hideLoading() {
        if (mView == null) {
            return;
        }

        mView.hideLoading();
    }


    private void showLoading() {
        if (mView == null) {
            return;
        }

        mView.showLoading();
    }

    private void createWalletOrder(long amount) {
        String description = mView.getContext().getString(R.string.deposit);
        Subscription subscription = mZaloPayRepository.createwalletorder(
                BuildConfig.ZALOPAY_APP_ID,
                amount,
                ETransactionType.TOPUP.toString(),
                mUser.zaloPayId,
                description)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CreateWalletOrderSubscriber());
        mSubscription.add(subscription);
    }

    public void initData(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        boolean showNotificationLinkCard = bundle.getBoolean(Constants.ARG_SHOW_NOTIFICATION_LINK_CARD, true);
        paymentWrapper.setShowNotificationLinkCard(showNotificationLinkCard);
    }

    private final class CreateWalletOrderSubscriber extends DefaultSubscriber<Order> {
        @Override
        public void onNext(Order order) {
            Timber.d("Create order for WalletTopup success: " + order);
            BalanceTopupPresenter.this.onCreateWalletOrderSuccess(order);
        }

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                // simply ignore the error
                // because it is handled from event subscribers
                return;
            }

            Timber.w(e, "onError " + e);
            BalanceTopupPresenter.this.onCreateWalletOrderError(e);
        }
    }

    private void onCreateWalletOrderError(Throwable e) {
        hideLoading();
        if (mView == null || mView.getContext() == null) {
            return;
        }
        if (e instanceof NetworkConnectionException) {
            mView.showNetworkErrorDialog();
        } else {
            String message = ErrorMessageFactory.create(mView.getContext(), e);
            mView.showError(message);
        }
    }

    private void onCreateWalletOrderSuccess(Order order) {
        paymentWrapper.payWithOrder(mView.getActivity(), order);
        hideLoading();
    }

    public void deposit(long amount) {
        showLoading();
        createWalletOrder(amount);
    }

    private class PaymentResponseListener extends DefaultPaymentResponseListener {
        @Override
        protected ILoadDataView getView() {
            return mView;
        }

        @Override
        public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
            if (mView == null || mView.getActivity() == null) {
                return;
            }
            mView.getActivity().setResult(Activity.RESULT_OK);
            mView.getActivity().finish();
        }

        @Override
        public void onResponseTokenInvalid() {
            if (mView == null) {
                return;
            }
            mApplicationSession.clearUserSession();
        }

        // Topup don't support add more money since this is action to add more money
//        @Override
//        public void onNotEnoughMoney() {
//        }
    }

    private class PaymentRedirectListener implements PaymentWrapper.IRedirectListener {
        @Override
        public void startUpdateProfileLevel(String walletTransId) {
            if (mView == null || mView.getFragment() == null) {
                return;
            }
            mNavigator.startUpdateProfile2ForResult(mView.getFragment(), walletTransId);
        }
    }
}

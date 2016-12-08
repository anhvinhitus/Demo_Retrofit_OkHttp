package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;
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
import vn.com.vng.zalopay.balancetopup.ui.view.IBalanceTopupView;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.DefaultPaymentResponseListener;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.service.PaymentWrapperBuilder;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.enumeration.ETransactionType;

/**
 * Created by longlv on 10/05/2016.
 * *
 */
public class BalanceTopupPresenter extends BaseUserPresenter implements IPresenter<IBalanceTopupView> {

    private final ZaloPayRepository mZaloPayRepository;
    private final Navigator mNavigator;
    private IBalanceTopupView mView;

    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    private final PaymentWrapper paymentWrapper;

    private User mUser;

    @Inject
    BalanceTopupPresenter(BalanceStore.Repository balanceRepository,
                          ZaloPayRepository zaloPayRepository,
                          TransactionStore.Repository transactionRepository,
                          Navigator navigator,
                          User user) {
        mZaloPayRepository = zaloPayRepository;
        mNavigator = navigator;
        mUser = user;
        paymentWrapper = new PaymentWrapperBuilder()
                .setBalanceRepository(balanceRepository)
                .setZaloPayRepository(zaloPayRepository)
                .setTransactionRepository(transactionRepository)
                .setResponseListener(new PaymentResponseListener())
                .build();
    }

    @Override
    public void setView(IBalanceTopupView iBalanceTopupView) {
        this.mView = iBalanceTopupView;
    }

    @Override
    public void destroyView() {
        unsubscribeIfNotNull(mCompositeSubscription);
        this.mView = null;
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {
    }

    private void hideLoadingView() {
        if (mView == null) {
            return;
        }

        mView.hideLoading();
    }

    private void showErrorView(String message) {
        if (mView == null) {
            return;
        }

        mView.showError(message);
    }

    private void createWalletOrder(long amount) {
        String description = mView.getContext().getString(R.string.deposit);
        Subscription subscription = mZaloPayRepository.createwalletorder(
                BuildConfig.ZALOPAY_APP_ID,
                amount,
                ETransactionType.TOPUP.toString(),
                mUser.zaloPayId,
                description)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CreateWalletOrderSubscriber());
        mCompositeSubscription.add(subscription);
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
        hideLoadingView();
        String message = ErrorMessageFactory.create(mView.getContext(), e);
        showErrorView(message);
    }

    private void onCreateWalletOrderSuccess(Order order) {
        paymentWrapper.payWithOrder(mView.getActivity(), order);
        hideLoadingView();
    }

    public void deposit(long amount) {
        createWalletOrder(amount);
    }

    private class PaymentResponseListener extends DefaultPaymentResponseListener {
        PaymentResponseListener() {
            super(mView);
        }

        @Override
        public void onResponseError(PaymentError paymentError) {
            if (mView == null) {
                return;
            }
            if (paymentError == PaymentError.ERR_CODE_INTERNET) {
                mView.showNetworkErrorDialog();
            }
            /*else {
                mView.showError("Lỗi xảy ra trong quá trình nạp tiền. Vui lòng thử lại sau.");
            }*/
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
            clearAndLogout();
        }

        // Topup don't support add more money since this is action to add more money
//        @Override
//        public void onNotEnoughMoney() {
//        }
    }
}

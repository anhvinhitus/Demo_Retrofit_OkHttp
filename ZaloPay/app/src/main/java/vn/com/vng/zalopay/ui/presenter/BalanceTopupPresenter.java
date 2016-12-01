package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
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
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.enumeration.ETransactionType;

/**
 * Created by longlv on 10/05/2016.
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
        paymentWrapper = new PaymentWrapper(balanceRepository, zaloPayRepository, transactionRepository, new PaymentWrapper.IViewListener() {
            @Override
            public Activity getActivity() {
                return mView.getActivity();
            }
        }, new PaymentWrapper.IResponseListener() {
            @Override
            public void onParameterError(String param) {
                if (mView == null) {
                    return;
                }

                switch (param) {
                    case "order":
                        mView.showError(mView.getContext().getString(R.string.order_invalid));
                        break;
                    case "uid":
                        mView.showError(mView.getContext().getString(R.string.user_invalid));
                        break;
                }
            }

            @Override
            public void onResponseError(PaymentError paymentError) {
                if (mView == null) {
                    return;
                }
                if (paymentError == PaymentError.ERR_CODE_INTERNET) {
                    mView.showWarning(mView.getContext().getString(R.string.exception_no_connection_try_again));
                }
                /*else {
                    mView.showError("Lỗi xảy ra trong quá trình nạp tiền. Vui lòng thử lại sau.");
                }*/
            }

            @Override
            public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                if (mView == null) {
                    return;
                }
                mView.getActivity().finish();
            }

            @Override
            public void onResponseTokenInvalid() {
                if (mView == null) {
                    return;
                }
                clearAndLogout();
            }

            @Override
            public void onPreComplete(boolean isSuccessful, String transId, String pAppTransId) {

            }

            @Override
            public void onAppError(String msg) {
                if (mView == null) {
                    return;
                }
                if (mView.getContext() != null) {
                    mView.showError(mView.getContext().getString(R.string.exception_generic));
                }
                mView.hideLoading();
            }

            @Override
            public void onNotEnoughMoney() {
                mNavigator.startDepositActivity(mView.getContext());
            }
        });
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

    private void showLoadingView() {
        if (mView == null) {
            return;
        }

        mView.showLoading();
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
                BuildConfig.PAYAPPID,
                amount,
                ETransactionType.TOPUP.toString(),
                mUser.zaloPayId,
                description)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CreateWalletOrderSubscriber());
        mCompositeSubscription.add(subscription);
    }

    private final class CreateWalletOrderSubscriber extends DefaultSubscriber<Order> {
        @Override
        public void onNext(Order order) {
            Timber.d("Create order for WalletTopup success: " + order);
            BalanceTopupPresenter.this.onCreateWalletOrderSuccess(order);
        }

        @Override
        public void onCompleted() {
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
        paymentWrapper.payWithOrder(order);
        hideLoadingView();
    }

    public void deposit(long amount) {
        createWalletOrder(amount);
    }

}

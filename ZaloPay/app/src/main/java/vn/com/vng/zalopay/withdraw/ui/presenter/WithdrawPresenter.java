package vn.com.vng.zalopay.withdraw.ui.presenter;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.exception.NetworkConnectionException;
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
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.vng.zalopay.withdraw.ui.view.IWithdrawView;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.enumeration.ETransactionType;

/**
 * Created by longlv on 11/08/2016.
 */
public class WithdrawPresenter extends BaseUserPresenter implements IPresenter<IWithdrawView> {
    private final int WITHDRAW_APPID = 2;
    private final BalanceStore.Repository mBalanceRepository;
    private final ZaloPayRepository mZaloPayRepository;
    private final Navigator mNavigator;

    private IWithdrawView mView;
    private PaymentWrapper paymentWrapper;
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    @Inject
    User mUser;

    @Inject
    public WithdrawPresenter(BalanceStore.Repository balanceRepository,
                             ZaloPayRepository zaloPayRepository,
                             TransactionStore.Repository transactionRepository,
                             Navigator navigator) {
        this.mBalanceRepository = balanceRepository;
        this.mZaloPayRepository = zaloPayRepository;
        this.mNavigator = navigator;
        paymentWrapper = new PaymentWrapperBuilder()
                .setBalanceRepository(balanceRepository)
                .setZaloPayRepository(zaloPayRepository)
                .setTransactionRepository(transactionRepository)
                .setResponseListener(new PaymentResponseListener())
                .setRedirectListener(new PaymentRedirectListener())
                .build();
    }

    public void continueWithdraw(long amount) {
        withdraw(amount);
    }

    private void withdraw(long amount) {
        if (amount <= 0 || mView == null) {
            return;
        }

        if (amount > mBalanceRepository.currentBalance()) {
            mView.showAmountError(mView.getContext().getString(R.string.withdraw_exceed_balance));
            return;
        }
        mView.showLoading();
        String description = mView.getContext().getString(R.string.withdraw_description);
        Subscription subscription = mZaloPayRepository.createwalletorder(WITHDRAW_APPID, amount, ETransactionType.WITHDRAW.toString(), mUser.zaloPayId, description)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CreateWalletOrderSubscriber());

        mCompositeSubscription.add(subscription);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (paymentWrapper == null || mView == null) {
            return;
        }

        paymentWrapper.onActivityResult(requestCode, resultCode, data);
    }

    private final class CreateWalletOrderSubscriber extends DefaultSubscriber<Order> {

        @Override
        public void onNext(Order order) {
            Timber.d("CreateWalletOrderSubscriber success " + order);
            WithdrawPresenter.this.onCreateWalletOrderSuccess(order);
        }

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                // simply ignore the error
                // because it is handled from event subscribers
                return;
            }

            Timber.e(e, "Server responses with error");
            WithdrawPresenter.this.onCreateWalletOrderError(e);
        }
    }

    private void onCreateWalletOrderError(Throwable e) {
        if (mView == null || mView.getContext() == null) {
            return;
        }

        mView.hideLoading();
        if (e instanceof NetworkConnectionException) {
            mView.showNetworkErrorDialog();
        } else {
            String message = ErrorMessageFactory.create(mView.getContext(), e);
            mView.showError(message);
        }
    }

    private void onCreateWalletOrderSuccess(Order order) {
        if (mView == null) {
            return;
        }

        paymentWrapper.withdraw(mView.getActivity(), order, mUser.displayName, mUser.avatar, String.valueOf(mUser.phonenumber), mUser.zalopayname);

        mView.hideLoading();
    }

    @Override
    public void setView(IWithdrawView iWithdrawView) {
        mView = iWithdrawView;
    }

    @Override
    public void destroyView() {
        mView = null;
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {
        mView = null;
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
            mView.hideLoading();

            super.onResponseError(paymentError);

            if (paymentError == PaymentError.ERR_TRANXSTATUS_NEED_LINKCARD) {
                mNavigator.startLinkCardActivity(mView.getActivity());
            }
        }

        @Override
        public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
            if (mView == null) {
                return;
            }

            if (mView.getActivity() != null) {
                mView.getActivity().setResult(Activity.RESULT_OK, null);
                mView.getActivity().finish();
            }
        }

        @Override
        public void onResponseTokenInvalid() {
            if (mView == null) {
                return;
            }
            mView.onTokenInvalid();
            clearAndLogout();
        }

        @Override
        public void onNotEnoughMoney() {
            if (mView == null) {
                return;
            }
            mNavigator.startDepositForResultActivity(mView.getFragment());
        }
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

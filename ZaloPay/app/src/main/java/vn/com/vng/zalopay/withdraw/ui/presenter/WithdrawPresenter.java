package vn.com.vng.zalopay.withdraw.ui.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.exception.GenericException;
import vn.com.vng.zalopay.data.exception.NetworkConnectionException;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.DefaultPaymentRedirectListener;
import vn.com.vng.zalopay.service.DefaultPaymentResponseListener;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.service.PaymentWrapperBuilder;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.vng.zalopay.withdraw.ui.view.IWithdrawView;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.enumeration.ETransactionType;

/**
 * Created by longlv on 11/08/2016.
 * *
 */
public class WithdrawPresenter extends AbstractPresenter<IWithdrawView> {
    private final int WITHDRAW_APPID = 2;
    private final BalanceStore.Repository mBalanceRepository;
    private final ZaloPayRepository mZaloPayRepository;
    private final Navigator mNavigator;

    private PaymentWrapper paymentWrapper;

    @Inject
    User mUser;

    private Context mContext;

    @Inject
    public WithdrawPresenter(Context context, BalanceStore.Repository balanceRepository,
                             ZaloPayRepository zaloPayRepository,
                             TransactionStore.Repository transactionRepository,
                             Navigator navigator
    ) {
        this.mBalanceRepository = balanceRepository;
        this.mZaloPayRepository = zaloPayRepository;
        this.mNavigator = navigator;
        this.mContext = context;

        paymentWrapper = new PaymentWrapperBuilder()
                .setBalanceRepository(balanceRepository)
                .setZaloPayRepository(zaloPayRepository)
                .setTransactionRepository(transactionRepository)
                .setResponseListener(new PaymentResponseListener())
                .setRedirectListener(new DefaultPaymentRedirectListener(mNavigator) {
                    @Override
                    public Object getContext() {
                        if (mView == null) {
                            return null;
                        }
                        return mView.getFragment();
                    }
                })
                .build();
    }

    public void withdraw(final long amount) {
        if (amount <= 0 || mView == null) {
            return;
        }

        Subscription subscription = mBalanceRepository.balanceLocal()
                .flatMap(new Func1<Long, Observable<Order>>() {
                    @Override
                    public Observable<Order> call(Long balance) {
                        if (amount > balance) {
                            return Observable.error(new GenericException(mContext.getString(R.string.withdraw_exceed_balance)));
                        } else {
                            return mZaloPayRepository.createwalletorder(WITHDRAW_APPID, amount, ETransactionType.WITHDRAW.toString(), mUser.zaloPayId, mContext.getString(R.string.withdraw_description));
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CreateWalletOrderSubscriber());

        mSubscription.add(subscription);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (paymentWrapper == null || mView == null) {
            return;
        }

        paymentWrapper.onActivityResult(requestCode, resultCode, data);
    }

    private final class CreateWalletOrderSubscriber extends DefaultSubscriber<Order> {

        @Override
        public void onStart() {
            if (mView != null) {
                mView.showLoading();
            }
        }

        @Override
        public void onNext(Order order) {
            Timber.d("CreateWalletOrderSubscriber success with order: %s", order);
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
        if (mView == null) {
            return;
        }

        mView.hideLoading();
        if (e instanceof NetworkConnectionException) {
            mView.showNetworkErrorDialog();
        } else if (e instanceof GenericException) {
            mView.showAmountError(e.getMessage());
        } else {
            String message = ErrorMessageFactory.create(mContext, e);
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

    private class PaymentResponseListener extends DefaultPaymentResponseListener {
        @Override
        protected ILoadDataView getView() {
            return mView;
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
    }

}

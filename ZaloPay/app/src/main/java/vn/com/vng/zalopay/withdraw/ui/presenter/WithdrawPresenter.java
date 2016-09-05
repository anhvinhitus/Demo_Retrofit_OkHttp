package vn.com.vng.zalopay.withdraw.ui.presenter;

import android.app.Activity;
import android.text.TextUtils;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.vng.zalopay.withdraw.ui.view.IWithdrawView;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.entity.enumeration.ETransactionType;

/**
 * Created by longlv on 11/08/2016.
 */
public class WithdrawPresenter extends BaseUserPresenter implements IPresenter<IWithdrawView> {
    private final int WITHDRAW_APPID = 2;

    private IWithdrawView mView;
    private User mUser;
    private PaymentWrapper paymentWrapper;
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    public WithdrawPresenter(User user) {
        this.mUser = user;
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
                if ("order".equalsIgnoreCase(param)) {
                    mView.showError(mView.getContext().getString(R.string.order_invalid));
                } else if ("uid".equalsIgnoreCase(param)) {
                    mView.showError(mView.getContext().getString(R.string.user_invalid));
                } else if ("token".equalsIgnoreCase(param)) {
                    mView.showError(mView.getContext().getString(R.string.order_invalid));
                } else if (!TextUtils.isEmpty(param)) {
                    mView.showError(param);
                }
                mView.hideLoading();
            }

            @Override
            public void onResponseError(PaymentError paymentError) {
                if (mView == null) {
                    return;
                }
                mView.hideLoading();
                if (paymentError == PaymentError.ZPC_TRANXSTATUS_NEED_LINKCARD) {
                    navigator.startLinkCardActivity(mView.getActivity());
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
            public void onPreComplete(boolean isSuccessful,String transId) {
                
            }

            @Override
            public void onNotEnoughMoney() {
                if (mView == null) {
                    return;
                }
                navigator.startDepositActivity(mView.getContext());
            }
        });
    }

    public void continueWithdraw(long amount) {
        //mView.showError("Chức năng sẽ sớm được ra mắt.");
        withdraw(amount);
    }

    private void withdraw(long amount) {
        if (amount <= 0 || mView == null) {
            return;
        }

        if (amount > balanceRepository.currentBalance()) {
            mView.showAmountError(mView.getContext().getString(R.string.withdraw_exceed_balance));
            return;
        }
        mView.showLoading();
        String description = mView.getContext().getString(R.string.txt_withdraw);
        Subscription subscription = zaloPayRepository.createwalletorder(WITHDRAW_APPID, amount, ETransactionType.WITHDRAW.toString(), mUser.zaloPayId, description)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CreateWalletOrderSubscriber());

        mCompositeSubscription.add(subscription);
    }

    private final class CreateWalletOrderSubscriber extends DefaultSubscriber<Order> {

        @Override
        public void onNext(Order order) {
            Timber.d("CreateWalletOrderSubscriber success " + order);
            WithdrawPresenter.this.onCreateWalletOrderSuccess(order);
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

            Timber.e(e, "Server responses with error");
            WithdrawPresenter.this.onCreateWalletOrderError(e);
        }
    }

    private void onCreateWalletOrderError(Throwable e) {
        if (mView == null) {
            return;
        }

        mView.hideLoading();
        String message = ErrorMessageFactory.create(mView.getContext(), e);
        mView.showError(message);
    }

    private void onCreateWalletOrderSuccess(Order order) {
        paymentWrapper.withdraw(order, mUser.displayName, mUser.avatar, String.valueOf(mUser.phonenumber), mUser.zalopayname);

        if (mView == null) {
            return;
        }

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
}

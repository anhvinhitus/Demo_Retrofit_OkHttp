package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.balancetopup.ui.view.IBalanceTopupView;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.mdl.error.PaymentError;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.entity.enumeration.ETransactionType;

/**
 * Created by longlv on 10/05/2016.
 */
public class BalanceTopupPresenter extends BaseZaloPayPresenter implements IPresenter<IBalanceTopupView> {

    private IBalanceTopupView mView;

    private Subscription subscriptionGetOrder;

    private final PaymentWrapper paymentWrapper;

    public BalanceTopupPresenter() {
        paymentWrapper = new PaymentWrapper(null, new PaymentWrapper.IViewListener() {
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
            public void onResponseError(int status) {
                if (mView == null) {
                    return;
                }
                if (status == PaymentError.ERR_CODE_INTERNET) {
                    mView.showError("Vui lòng kiểm tra kết nối mạng và thử lại.");
                }
                /*else {
                    mView.showError("Lỗi xảy ra trong quá trình nạp tiền. Vui lòng thử lại sau.");
                }*/
            }

            @Override
            public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                updateTransaction();
                updateBalance();

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
                mView.onTokenInvalid();
            }

            @Override
            public void onResponseCancel() {

            }
        });
    }

    @Override
    public void setView(IBalanceTopupView iBalanceTopupView) {
        this.mView = iBalanceTopupView;
    }

    @Override
    public void destroyView() {
        this.mView = null;
//        this.zpPaymentListener = null;
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {
        this.destroyView();
        this.unsubscribe();
    }

    private void unsubscribe() {
        unsubscribeIfNotNull(subscriptionGetOrder);
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
        if (userConfig == null || userConfig.getCurrentUser() == null) {
            return;
        }

        String description = mView.getContext().getString(R.string.deposit);
        subscriptionGetOrder = zaloPayRepository.createwalletorder(BuildConfig.PAYAPPID, amount, ETransactionType.TOPUP.toString(), userConfig.getCurrentUser().uid, description)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CreateWalletOrderSubscriber());
    }

    private final class CreateWalletOrderSubscriber extends DefaultSubscriber<Order> {
        public CreateWalletOrderSubscriber() {
        }

        @Override
        public void onNext(Order order) {
            Timber.d("login success " + order);
            BalanceTopupPresenter.this.onCreateWalletOrderSuccess(order);
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Timber.w(e, "onError " + e);
            if (e != null && e instanceof BodyException) {
                if (((BodyException) e).errorCode == NetworkError.TOKEN_INVALID) {
                    clearAndLogout();
                    return;
                }
            }
            BalanceTopupPresenter.this.onCreateWalletOrderError(e);
        }
    }

    private void onCreateWalletOrderError(Throwable e) {
        Timber.tag("onCreateWalletOrderError").d("session =========" + e);
        hideLoadingView();
        String message = ErrorMessageFactory.create(mView.getContext(), e);
        showErrorView(message);
    }

    private void onCreateWalletOrderSuccess(Order order) {
        Timber.tag("onCreateWalletOrderSuccess").d("session =========" + order.getItem());
//        pay(order);
        paymentWrapper.payWithOrder(order);
        hideLoadingView();
    }

    public void deposit(long amount) {
        if (amount <= 0) {
            showErrorView("Số tiền phải là bội của 10.000 VND");
            return;
        }
        createWalletOrder(amount);
    }

}

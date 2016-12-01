package vn.com.vng.zalopay.linkcard.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;

/**
 * Created by longlv on 10/25/16.
 * Contains linkCard function
 */

abstract class AbsLinkCardPresenter extends BaseUserPresenter {

    CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    private PaymentWrapper paymentWrapper;
    private Navigator mNavigator;

    @Inject
    User user;

    @Inject
    SharedPreferences mSharedPreferences;

    abstract Activity getActivity();

    abstract Context getContext();

    abstract void onTokenInvalid();

    abstract void onPreComplete();

    abstract void onAddCardSuccess(DMappedCard mappedCreditCard);

    abstract void showLoadingView();

    abstract void hideLoadingView();

    abstract void showErrorView(String message);

    abstract void showWarningView(String message);

    AbsLinkCardPresenter(ZaloPayRepository zaloPayRepository,
                         Navigator navigator,
                         BalanceStore.Repository balanceRepository,
                         TransactionStore.Repository transactionRepository) {
        mNavigator = navigator;
        paymentWrapper = new PaymentWrapper(balanceRepository, zaloPayRepository, transactionRepository, new PaymentWrapper.IViewListener() {
            @Override
            public Activity getActivity() {
                return AbsLinkCardPresenter.this.getActivity();
            }
        }, new PaymentWrapper.IResponseListener() {
            @Override
            public void onParameterError(String param) {
                showErrorView(param);
            }

            @Override
            public void onResponseError(PaymentError paymentError) {
                if (paymentError == PaymentError.ERR_CODE_INTERNET) {
                    showWarningView(getContext().getString(R.string.exception_no_connection_try_again));
                }
            }

            @Override
            public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                ZPWPaymentInfo paymentInfo = zpPaymentResult.paymentInfo;
                if (paymentInfo == null) {
                    Timber.d("onResponseSuccess paymentInfo null");
                    return;
                }
                onAddCardSuccess(paymentInfo.mappedCreditCard);
            }

            @Override
            public void onResponseTokenInvalid() {
                onTokenInvalid();
                clearAndLogout();
            }

            @Override
            public void onAppError(String msg) {
                showErrorView(msg);
            }

            @Override
            public void onPreComplete(boolean isSuccessful, String tId, String pAppTransId) {
                Timber.d("onPreComplete isSuccessful [%s]", isSuccessful);
                if (isSuccessful) {
                    AbsLinkCardPresenter.this.onPreComplete();
                }
            }

            @Override
            public void onNotEnoughMoney() {

            }
        });
    }

    void addLinkCard() {
        if (getContext() == null) {
            return;
        }
        if (user.profilelevel < 2) {
            mNavigator.startUpdateProfileLevel2Activity(getContext());
        } else {
            paymentWrapper.linkCard();
            hideLoadingView();
        }
    }
}

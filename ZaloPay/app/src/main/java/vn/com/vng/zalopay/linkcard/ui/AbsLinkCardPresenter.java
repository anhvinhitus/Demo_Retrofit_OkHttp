package vn.com.vng.zalopay.linkcard.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
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
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.business.entity.enumeration.ETransactionType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * Created by longlv on 10/25/16.
 * Contains linkCard function
 */

abstract class AbsLinkCardPresenter extends BaseUserPresenter {
    private final String FIRST_OPEN_SAVE_CARD_KEY = "1st_open_save_card";

    CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    private PaymentWrapper paymentWrapper;
    private ZaloPayRepository zaloPayRepository;
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

    AbsLinkCardPresenter(ZaloPayRepository zaloPayRepository,
                         Navigator navigator,
                         BalanceStore.Repository balanceRepository,
                         TransactionStore.Repository transactionRepository) {
        this.zaloPayRepository = zaloPayRepository;
        mNavigator = navigator;
        paymentWrapper = new PaymentWrapper(balanceRepository, this.zaloPayRepository, transactionRepository, new PaymentWrapper.IViewListener() {
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
                    showErrorView("Vui lòng kiểm tra kết nối mạng và thử lại.");
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
//        } else if (!isOpenedIntroActivity()) {
//            mCardView.startIntroActivityForResult();
        } else {
            long value = 10000;
            try {
                value = CShareData.getInstance().getLinkCardValue();
            } catch (Exception e) {
                Timber.w(e, "getLinkCardValue exception [%s]", e.getMessage());
            }
            showLoadingView();
            String description = getContext().getString(R.string.save_card_description);
            Subscription subscription = zaloPayRepository.createwalletorder(BuildConfig.PAYAPPID, value, ETransactionType.LINK_CARD.toString(), user.zaloPayId, description)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CreateWalletOrderSubscriber());
            mCompositeSubscription.add(subscription);
        }
    }

    private final class CreateWalletOrderSubscriber extends DefaultSubscriber<Order> {
        CreateWalletOrderSubscriber() {
        }

        @Override
        public void onNext(Order order) {
            Timber.d("CreateWalletOrderSubscriber onNext order: [%s]" + order);
            AbsLinkCardPresenter.this.onCreateWalletOrderSuccess(order);
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

            Timber.w(e, "CreateWalletOrderSubscriber onError exception: [%s]" + e);
            AbsLinkCardPresenter.this.onCreateWalletOrderError(e);
        }
    }

    private void onCreateWalletOrderError(Throwable e) {
        Timber.d("onCreateWalletOrderError exception: [%s]" + e);
        hideLoadingView();
        String message = ErrorMessageFactory.create(getContext(), e);
        showErrorView(message);
    }

    private void onCreateWalletOrderSuccess(Order order) {
        Timber.d("onCreateWalletOrderSuccess order: [%s]", order);
        paymentWrapper.linkCard(order);
        hideLoadingView();
    }

    void setOpenedIntroActivity() {
        mSharedPreferences.edit().putBoolean(FIRST_OPEN_SAVE_CARD_KEY, true).apply();
    }

    private boolean isOpenedIntroActivity() {
        return mSharedPreferences.getBoolean(FIRST_OPEN_SAVE_CARD_KEY, false);
    }
}

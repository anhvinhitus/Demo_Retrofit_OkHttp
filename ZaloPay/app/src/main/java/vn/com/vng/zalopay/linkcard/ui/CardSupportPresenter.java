package vn.com.vng.zalopay.linkcard.ui;

import android.app.Activity;

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
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.enumeration.ETransactionType;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * Created by longlv on 10/22/16.
 * *
 */
class CardSupportPresenter extends BaseUserPresenter implements IPresenter<ICardSupportView> {

    private ICardSupportView mLinkCardView;
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    private PaymentWrapper paymentWrapper;
    private ZaloPayRepository zaloPayRepository;
    private Navigator mNavigator;

    @Inject
    User user;

    @Inject
    CardSupportPresenter(ZaloPayRepository zaloPayRepository,
                         Navigator navigator,
                         BalanceStore.Repository balanceRepository,
                         TransactionStore.Repository transactionRepository) {
        this.zaloPayRepository = zaloPayRepository;
        mNavigator = navigator;
        paymentWrapper = new PaymentWrapper(balanceRepository, this.zaloPayRepository, transactionRepository, new PaymentWrapper.IViewListener() {
            @Override
            public Activity getActivity() {
                return mLinkCardView.getActivity();
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
//                ZPWPaymentInfo paymentInfo = zpPaymentResult.paymentInfo;
//                if (paymentInfo == null) {
//                    Timber.d("onResponseSuccess paymentInfo null");
//                    return;
//                }
//                mLinkCardView.onAddCardSuccess(paymentInfo.mappedCreditCard);
            }

            @Override
            public void onResponseTokenInvalid() {
                mLinkCardView.onTokenInvalid();
                clearAndLogout();
            }

            @Override
            public void onAppError(String msg) {
                if (mLinkCardView == null || mLinkCardView.getContext() == null) {
                    return;
                }
                showErrorView(mLinkCardView.getContext().getString(R.string.exception_generic));
            }

            @Override
            public void onPreComplete(boolean isSuccessful, String tId, String pAppTransId) {
                Timber.d("onPreComplete isSuccessful [%s]", isSuccessful);
                if (isSuccessful) {
                    mLinkCardView.onPreComplete();
                }
            }

            @Override
            public void onNotEnoughMoney() {

            }
        });
    }

    @Override
    public void setView(ICardSupportView iLinkCardView) {
        mLinkCardView = iLinkCardView;
    }

    @Override
    public void destroyView() {
        mLinkCardView = null;
        unsubscribeIfNotNull(mCompositeSubscription);
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {
        //release cache
        CShareData.dispose();
        GlobalData.initApplication(null);
    }

    void addLinkCard() {
        if (user.profilelevel < 2) {
            mNavigator.startUpdateProfileLevel2Activity(mLinkCardView.getContext());
//        } else if (!isOpenedIntroActivity()) {
//            mLinkCardView.startIntroActivityForResult();
        } else {
            long value = 10000;
            if (mLinkCardView.getActivity() != null) {
                try {
                    value = CShareData.getInstance().getLinkCardValue();
                } catch (Exception e) {
                    Timber.e(e, "getLinkCardValue exception [%s]", e.getMessage());
                }
            }
            showLoadingView();
            String description = mLinkCardView.getContext().getString(R.string.save_card_description);
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
            CardSupportPresenter.this.onCreateWalletOrderSuccess(order);
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
            CardSupportPresenter.this.onCreateWalletOrderError(e);
        }
    }

    private void onCreateWalletOrderError(Throwable e) {
        Timber.d("onCreateWalletOrderError exception: [%s]" + e);
        hideLoadingView();
        String message = ErrorMessageFactory.create(mLinkCardView.getContext(), e);
        showErrorView(message);
    }

    private void onCreateWalletOrderSuccess(Order order) {
        Timber.d("onCreateWalletOrderSuccess order: [%s]", order);
        paymentWrapper.linkCard(order);
        hideLoadingView();
    }

    private void showLoadingView() {
        if (mLinkCardView == null) {
            return;
        }
        mLinkCardView.showLoading();
    }

    private void hideLoadingView() {
        if (mLinkCardView == null) {
            return;
        }
        mLinkCardView.hideLoading();
    }

    private void showErrorView(String message) {
        if (mLinkCardView == null) {
            return;
        }
        mLinkCardView.hideLoading();
        mLinkCardView.showError(message);
    }
}

package vn.com.vng.zalopay.linkcard.ui;

import android.app.Activity;
import android.content.Context;

import javax.inject.Inject;

import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;

/**
 * Created by longlv on 10/22/16.
 * *
 */
class CardSupportPresenter extends AbstractLinkCardPresenter<ICardSupportView> {
    @Inject
    User user;

    @Inject
    CardSupportPresenter(ZaloPayRepository zaloPayRepository,
                         Navigator navigator,
                         BalanceStore.Repository balanceRepository,
                         TransactionStore.Repository transactionRepository) {
        super(zaloPayRepository, navigator, balanceRepository, transactionRepository);
    }

    @Override
    Activity getActivity() {
        if (mView == null) {
            return null;
        }
        return mView.getActivity();
    }

    @Override
    Context getContext() {
        if (mView == null) {
            return null;
        }
        return mView.getContext();
    }

    @Override
    void onTokenInvalid() {
        if (mView == null) {
            return;
        }
        mView.onTokenInvalid();
    }

    @Override
    void onPreComplete() {
        if (mView == null) {
            return;
        }
        mView.onPreComplete();
    }

    @Override
    void onAddCardSuccess(DMappedCard mappedCreditCard) {

    }

    @Override
    protected void showLoadingView() {
        if (mView == null) {
            return;
        }
        mView.showLoading();
    }

    @Override
    protected void hideLoadingView() {
        if (mView == null) {
            return;
        }
        mView.hideLoading();
    }

    @Override
    protected void showErrorView(String message) {
        if (mView == null) {
            return;
        }
        mView.hideLoading();
        mView.showError(message);
    }

    @Override
    void showNetworkErrorDialog() {
        if (mView == null) {
            return;
        }
        mView.hideLoading();
        mView.showNetworkErrorDialog();
    }
}

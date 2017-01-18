package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import javax.inject.Inject;

import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.bank.models.BankAccount;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;

/**
 * Created by longlv on 1/17/17.
 * *
 */
class LinkAccountPresenter extends AbstractLinkCardPresenter<ILinkAccountView> {

    @Inject
    LinkAccountPresenter(ZaloPayRepository zaloPayRepository,
                         Navigator navigator,
                         BalanceStore.Repository balanceRepository,
                         TransactionStore.Repository transactionRepository,
                         User user,
                         SharedPreferences sharedPreferences, EventBus eventBus
    ) {
        super(zaloPayRepository, navigator, balanceRepository, transactionRepository,
                user, sharedPreferences, eventBus);
    }

    private void getListAccount() {
        showLoadingView();

    }

    @Override
    public void resume() {
        getListAccount();
    }

    private void onGetLinkAccountSuccess(List<BankAccount> list) {
        hideLoadingView();
        mView.setData(list);
    }

    void removeLinkAccount(BankAccount bankCard) {

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
    void onPreComplete() {

    }

    @Override
    void onAddCardSuccess(DMappedCard mappedCreditCard) {
        if (mView == null) {
            return;
        }
        mView.onAddAccountSuccess(mappedCreditCard);
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

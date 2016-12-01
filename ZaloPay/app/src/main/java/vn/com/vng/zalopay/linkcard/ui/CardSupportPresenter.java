package vn.com.vng.zalopay.linkcard.ui;

import android.app.Activity;
import android.content.Context;

import javax.inject.Inject;

import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * Created by longlv on 10/22/16.
 * *
 */
class CardSupportPresenter extends AbsLinkCardPresenter implements IPresenter<ICardSupportView> {

    private ICardSupportView mCardSupportView;

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
    public void setView(ICardSupportView iLinkCardView) {
        mCardSupportView = iLinkCardView;
    }

    @Override
    public void destroyView() {
        mCardSupportView = null;
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

    @Override
    Activity getActivity() {
        if (mCardSupportView == null) {
            return null;
        }
        return mCardSupportView.getActivity();
    }

    @Override
    Context getContext() {
        if (mCardSupportView == null) {
            return null;
        }
        return mCardSupportView.getContext();
    }

    @Override
    void onTokenInvalid() {
        if (mCardSupportView == null) {
            return;
        }
        mCardSupportView.onTokenInvalid();
    }

    @Override
    void onPreComplete() {
        if (mCardSupportView == null) {
            return;
        }
        mCardSupportView.onPreComplete();
    }

    @Override
    void onAddCardSuccess(DMappedCard mappedCreditCard) {

    }

    @Override
    protected void showLoadingView() {
        if (mCardSupportView == null) {
            return;
        }
        mCardSupportView.showLoading();
    }

    @Override
    protected void hideLoadingView() {
        if (mCardSupportView == null) {
            return;
        }
        mCardSupportView.hideLoading();
    }

    @Override
    protected void showErrorView(String message) {
        if (mCardSupportView == null) {
            return;
        }
        mCardSupportView.hideLoading();
        mCardSupportView.showError(message);
    }

    @Override
    void showWarningView(String message) {
        if (mCardSupportView == null) {
            return;
        }
        mCardSupportView.hideLoading();
        mCardSupportView.showWarningView(message);
    }
}

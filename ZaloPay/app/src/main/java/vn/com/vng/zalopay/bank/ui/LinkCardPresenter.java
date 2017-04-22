package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.BankUtils;
import vn.com.vng.zalopay.bank.models.BankAccount;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.BankCard;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.event.TokenPaymentExpiredEvent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.base.ZPWRemoveMapCardParams;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.listener.ZPWRemoveMapCardListener;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;

/**
 * Created by AnhHieu on 5/11/16.
 * *
 */
public class LinkCardPresenter extends AbstractLinkCardPresenter<ILinkCardView> {

    @Inject
    LinkCardPresenter(ZaloPayRepository zaloPayRepository,
                      Navigator navigator,
                      BalanceStore.Repository balanceRepository,
                      TransactionStore.Repository transactionRepository,
                      User user, EventBus eventBus) {
        super(zaloPayRepository, navigator, balanceRepository, transactionRepository, user, eventBus);
    }

    void getListCard() {
        showLoadingView();
        Subscription subscription = ObservableHelper.makeObservable(() -> {
            List<DMappedCard> mapCardLis = CShareDataWrapper.getMappedCardList(mUser.zaloPayId);
            return transformBankCard(mapCardLis);
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LinkCardSubscriber());
        mSubscription.add(subscription);
    }

    @Override
    public void resume() {
        if (mView != null && mView.getUserVisibleHint()) {
            getListCard();
        }
    }

    private void onGetLinkCardSuccess(List<BankCard> list) {
        hideLoadingView();
        mView.setData(list);
    }

    void removeLinkCard(BankCard bankCard) {
        showLoadingView();

        ZPWRemoveMapCardParams params = new ZPWRemoveMapCardParams();
        DMappedCard mapCard = new DMappedCard();
        mapCard.cardname = bankCard.cardname;
        mapCard.first6cardno = bankCard.first6cardno;
        mapCard.last4cardno = bankCard.last4cardno;
        mapCard.bankcode = bankCard.bankcode;

        if (mUser == null) {
            showErrorView("Thông tin người dùng không hợp lệ.");
            return;
        }
        params.accessToken = mUser.accesstoken;
        params.userID = String.valueOf(mUser.zaloPayId);
        params.mapCard = mapCard;

        SDKApplication.removeCardMap(params, new RemoveMapCardListener());
    }

    private final class RemoveMapCardListener implements ZPWRemoveMapCardListener {
        @Override
        public void onSuccess(DMappedCard mapCard) {
            Timber.d("removed map card: %s", mapCard);
            if (mView == null) {
                return;
            }

            hideLoadingView();
            if (mapCard != null) {
                BankCard bankCard = new BankCard(mapCard.cardname, mapCard.first6cardno,
                        mapCard.last4cardno, mapCard.bankcode);
                mView.removeData(bankCard);
            }
            showNotificationDialog(R.string.txt_remove_link_successfully);
        }

        @Override
        public void onError(BaseResponse pMessage) {
            Timber.d("Remove map card error : message [%s]", pMessage);
            if (mView == null) {
                return;
            }

            hideLoadingView();
            if (pMessage == null) {
                if (NetworkHelper.isNetworkAvailable(mView.getContext())) {
                    showErrorView(mView.getContext().getString(R.string.error_message_link_card_unknown_error));
                } else {
                    showNetworkErrorDialog();
                }
            } else if (pMessage.returncode == NetworkError.TOKEN_INVALID) {
                mEventBus.postSticky(new TokenPaymentExpiredEvent());
            } else if (!TextUtils.isEmpty(pMessage.returnmessage)) {
                Timber.d("err removed map card %s", pMessage.returnmessage);
                showErrorView(pMessage.returnmessage);
            }
        }
    }

    private final class LinkCardSubscriber extends DefaultSubscriber<List<BankCard>> {
        LinkCardSubscriber() {
        }

        @Override
        public void onError(Throwable e) {
            hideLoadingView();
            if (ResponseHelper.shouldIgnoreError(e)) {
                // simply ignore the error
                // because it is handled from event subscribers
                return;
            }
        }

        @Override
        public void onNext(List<BankCard> bankCards) {
            LinkCardPresenter.this.onGetLinkCardSuccess(bankCards);
        }
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
    void onAddCardSuccess(DBaseMap mapBank) {
        if (mView == null || mapBank == null) {
            return;
        }
        if (mapBank instanceof DMappedCard) {
            mView.onAddCardSuccess(mapBank);
        } else if (mapBank instanceof DBankAccount) {
            mView.gotoTabLinkAccAndReloadLinkedAcc();
        }
    }

    @Override
    void onLoadIconFontSuccess() {
        if (mView != null) {
            mView.refreshLinkedCard();
        }
    }

    @Override
    void onDownloadPaymentSDKComplete() {
        if (mView != null) {
            mView.refreshBanksSupport();
        }
    }

    @Override
    public void onErrorLinkCardButInputBankAccount(DBaseMap bankInfo) {
        super.onErrorLinkCardButInputBankAccount(bankInfo);
        if (bankInfo == null) {
            return;
        }
        Timber.d("Start LinkAccount with bank code [%s]", bankInfo.bankcode);
        getLinkedBankAccount(new GetLinkedBankAccSubscriber(bankInfo.bankcode));
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

    private void showErrorView(int msgResource) {
        if (mView == null || mView.getContext() == null) {
            return;
        }
        mView.hideLoading();
        mView.showError(getContext().getString(msgResource));
    }

    private void showNotificationDialog(int msgResource) {
        if (mView == null || mView.getContext() == null) {
            return;
        }
        mView.hideLoading();
        mView.showNotificationDialog(getContext().getString(msgResource));
    }

    @Override
    void showNetworkErrorDialog() {
        if (mView == null) {
            return;
        }
        mView.hideLoading();
        mView.showNetworkErrorDialog();
    }

    @Override
    void showRetryDialog(String message, ZPWOnEventConfirmDialogListener listener) {
        if (mView == null) {
            return;
        }
        mView.showRetryDialog(message, listener);
    }

    @Override
    void onUpdateVersion(boolean forceUpdate, String latestVersion, String message) {
        if (mView == null) {
            return;
        }
        mView.onUpdateVersion(forceUpdate, latestVersion, message);
    }

    @Override
    void onGetCardSupportSuccess(ArrayList<ZPCard> cardSupportList) {
        Timber.d("on Get Card Support Success");
        if (cardSupportList == null || cardSupportList.size() <= 0) {
            return;
        }
        ArrayList<ZPCard> cards = new ArrayList<>();
        for (ZPCard card : cardSupportList) {
            if (card == null || card.isBankAccount()) {
                continue;
            }
            cards.add(card);
        }
        if (Lists.isEmptyOrNull(cards)) {
            showErrorView(R.string.link_card_bank_support_empty);
        } else {
            if (mView != null) {
                mView.showListBankSupportDialog(cards);
            }
        }
    }

    private class GetLinkedBankAccSubscriber extends DefaultSubscriber<List<BankAccount>> {
        private String mBankCode;

        GetLinkedBankAccSubscriber(String bankCode) {
            super();
            mBankCode = bankCode;
        }

        @Override
        public void onNext(List<BankAccount> bankAccounts) {
            hideLoadingView();
            if (checkLinkedBankAccount(bankAccounts, mBankCode)) {
                String bankName = BankUtils.getBankName(mBankCode);
                String message;
                if (!TextUtils.isEmpty(bankName)) {
                    message = String.format(getString(R.string.bank_account_has_linked),
                            bankName);
                } else {
                    message = getString(R.string.bank_account_has_linked_this_bank);
                }
                if (mView != null) {
                    mView.gotoTabLinkAccAndShowDialog(message);
                }
            } else {
                linkAccount(mBankCode);
            }
        }

        @Override
        public void onError(Throwable e) {
            hideLoadingView();
            linkAccount(mBankCode);
        }
    }

    private String getString(@StringRes int stringResource) {
        if (mView == null || mView.getContext() == null) {
            return "";
        }
        return mView.getContext().getString(stringResource);
    }
}

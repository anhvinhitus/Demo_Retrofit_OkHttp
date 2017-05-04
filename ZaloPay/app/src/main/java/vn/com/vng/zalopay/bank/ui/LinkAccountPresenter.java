package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.models.BankAccount;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.event.LoadIconFontEvent;
import vn.com.vng.zalopay.event.RefreshBankAccountEvent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.business.entity.enumeration.ECardType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;

/**
 * Created by longlv on 1/17/17.
 * Logic of LinkAccountFragment.
 */
class LinkAccountPresenter extends AbstractLinkCardPresenter<ILinkAccountView> {

    @Inject
    LinkAccountPresenter(ZaloPayRepository zaloPayRepository,
                         Navigator navigator,
                         BalanceStore.Repository balanceRepository,
                         TransactionStore.Repository transactionRepository,
                         User user, EventBus eventBus) {
        super(zaloPayRepository, navigator, balanceRepository, transactionRepository, user, eventBus);
    }

    void linkAccountIfNotExist(ZPCard zpCard) {
        List<DBankAccount> mapCardLis = CShareDataWrapper.getMapBankAccountList(mUser.zaloPayId);
        if (checkLinkedBankAccount(transformBankAccount(mapCardLis), zpCard.getCardCode())) {
            showAccountHasLinked(zpCard);
        } else {
            linkAccount(zpCard);
        }
    }

    void refreshLinkedBankAccount() {
        List<BankAccount> bankAccounts = getLinkedBankAccount();
        mView.refreshLinkedAccount(bankAccounts);
        checkSupportVcbOnly(bankAccounts);
    }

    @Override
    public void resume() {
    }

    private boolean linkedVcbAccount(List<BankAccount> listLinkedAccount) {
        for (BankAccount bankAccount : listLinkedAccount) {
            if (bankAccount == null || TextUtils.isEmpty(bankAccount.mBankCode)) {
                continue;
            }
            boolean linkedVcbAccount = ECardType.PVCB.toString().equals(bankAccount.mBankCode);
            Timber.d("Linked vcb [%s], bankCode [%s]", linkedVcbAccount, bankAccount.mBankCode);
            if (linkedVcbAccount) {
                return true;
            }
        }
        return false;
    }

    private List<ZPCard> getBanksSupportLinkAccount(List<ZPCard> bankList) {
        if (Lists.isEmptyOrNull(bankList)) {
            return Collections.emptyList();
        }
        List<ZPCard> bankSupportLinkAccount = new ArrayList<>();
        for (ZPCard card : bankList) {
            if (card == null || !card.isBankAccount()) {
                continue;
            }
            bankSupportLinkAccount.add(card);
        }
        return bankSupportLinkAccount;
    }

    private void checkSupportVcbOnly(List<BankAccount> listLinkedAccount) {
        if (Lists.isEmptyOrNull(listLinkedAccount) || !linkedVcbAccount(listLinkedAccount)) {
            return;
        }
        DefaultSubscriber subscriber = new DefaultSubscriber<List<ZPCard>>() {
            @Override
            public void onCompleted() {
                unsubscribe();
            }

            @Override
            public void onError(Throwable e) {
                unsubscribe();
                Timber.d("Get card support to check support vcb only error : message [%s]", e.getMessage());
            }

            @Override
            public void onNext(List<ZPCard> cardList) {
                unsubscribe();
                if (mView == null) {
                    return;
                }
                List<ZPCard> banksSupportLinkAcc = getBanksSupportLinkAccount(cardList);
                if (!Lists.isEmptyOrNull(banksSupportLinkAcc)
                        && banksSupportLinkAcc.size() == 1
                        && ECardType.PVCB.toString().equals(banksSupportLinkAcc.get(0).getCardCode())) {
                    mView.showSupportVcbOnly();
                } else {
                    mView.hideSupportVcbOnly();
                }
            }
        };

        getListBankSupport(subscriber);
    }

    void removeLinkAccount(BankAccount bankAccount) {
        if (paymentWrapper == null || mView == null || bankAccount == null) {
            return;
        }
        paymentWrapper.unLinkAccount(mView.getActivity(), bankAccount.mBankCode);
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
    void onResponseSuccessFromSDK(ZPPaymentResult zpPaymentResult) {
        ZPWPaymentInfo paymentInfo = zpPaymentResult.paymentInfo;
        if (paymentInfo == null) {
            Timber.d("PaymentSDK response success but paymentInfo null");
            return;
        }
        if (paymentInfo.linkAccInfo != null) {
            if (paymentInfo.linkAccInfo.isLinkAcc()) {
                onAddAccountSuccess(paymentInfo.mapBank);
            } else if (paymentInfo.linkAccInfo.isUnlinkAcc()) {
                onRemoveAccountSuccess(paymentInfo.mapBank);
            }
        }
    }

    private void onRemoveAccountSuccess(DBaseMap bankAccount) {
        if (mView == null || !(bankAccount instanceof DBankAccount)) {
            return;
        }

        DBankAccount dBankAccount = ((DBankAccount) bankAccount);
        mView.removeData(transformBankAccount(dBankAccount));
    }

    private void onAddAccountSuccess(DBaseMap mappedCreditCard) {
        if (mView == null || !(mappedCreditCard instanceof DBankAccount)) {
            return;
        }

        DBankAccount dBankAccount = ((DBankAccount) mappedCreditCard);
        BankAccount bankAccount = transformBankAccount(dBankAccount);
        mView.insertData(bankAccount);
        checkSupportVcbOnly(Collections.singletonList(bankAccount));
        if (mPayAfterLinkBank) {
            mView.showConfirmPayAfterLinkBank();
        }
    }

    @Override
    void onLoadIconFontSuccess() {
        if (mView != null) {
            mView.refreshLinkedAccount();
        }
    }

    @Override
    void onDownloadPaymentSDKComplete() {
        if (mView != null) {
            mView.refreshBanksSupport();
        }
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
    void onGetCardSupportSuccess(List<ZPCard> cardSupportList) {
        if (cardSupportList == null || cardSupportList.size() <= 0) {
            return;
        }
        ArrayList<ZPCard> cards = new ArrayList<>();
        for (ZPCard card : cardSupportList) {
            if (card == null || !card.isBankAccount()) {
                continue;
            }
            cards.add(card);
        }
        if (Lists.isEmptyOrNull(cards)) {
            showErrorView(R.string.link_account_bank_support_empty);
        } else if (cards.size() == 1) {
            linkAccount(cards.get(0));
        } else {
            if (mView != null) {
                mView.showListBankDialog(cards);
            }
        }
    }

    private void showAccountHasLinked(ZPCard zpCard) {
        hideLoadingView();
        if (mView == null || mView.getContext() == null) {
            return;
        }

        String message = String.format(mView.getContext().getString(R.string.bank_account_has_linked),
                zpCard.getCardLogoName());
        mView.showError(message);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadIconFontSuccess(LoadIconFontEvent event) {
        Timber.d("Load icon font success.");
        if (event != null && mView != null) {
            mView.refreshLinkedAccount();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshBankAccount(RefreshBankAccountEvent event) {
        Timber.d("Refresh bank account if PaymentSDk reload successfully [%s]", event.mIsError);
        if (!event.mIsError) {
            refreshLinkedBankAccount();
        }
    }
}

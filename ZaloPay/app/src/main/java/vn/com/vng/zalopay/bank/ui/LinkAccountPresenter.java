package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.models.BankAccount;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.event.LoadIconFontEvent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
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
                         User user,
                         SharedPreferences sharedPreferences, EventBus eventBus) {
        super(zaloPayRepository, navigator, balanceRepository, transactionRepository,
                user, sharedPreferences, eventBus);
    }

    void getMapBankAccount() {
        showLoadingView();
        Subscription subscription = ObservableHelper.makeObservable(() -> {
            List<DBankAccount> mapCardLis = CShareDataWrapper.getMapBankAccountList(mUser.zaloPayId);
            return transform(mapCardLis);
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new GetLinkedAccountSubscriber());
        mSubscription.add(subscription);
    }

    void linkAccountIfNotExist(ZPCard zpCard) {
        Subscription subscription = ObservableHelper.makeObservable(() -> {
            List<DBankAccount> mapCardLis = CShareDataWrapper.getMapBankAccountList(mUser.zaloPayId);
            return transform(mapCardLis);
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LinkAccountIfNotLinkedSubscriber(zpCard));
        mSubscription.add(subscription);
    }

    private void linkAccount(ZPCard zpCard) {
        if (paymentWrapper == null || mView == null || zpCard == null) {
            return;
        }
        Timber.d("Link account, card code [%s]", zpCard.getCardCode());
        paymentWrapper.linkAccount(getActivity(), zpCard.getCardCode());
        hideLoadingView();
    }

    private List<BankAccount> transform(List<DBankAccount> bankAccounts) {
        if (Lists.isEmptyOrNull(bankAccounts)) return Collections.emptyList();

        List<BankAccount> list = new ArrayList<>();
        for (DBankAccount dBankAccount : bankAccounts) {
            BankAccount bankAccount = transform(dBankAccount);
            if (bankAccount != null) {
                list.add(bankAccount);
            }
        }
        return list;
    }

    private BankAccount transform(DBankAccount dBankAccount) {
        if (dBankAccount == null) {
            return null;
        }

        //bankCode [ZPVCB] cardKey[160525000004003ZPVCB] cardType[ZPVCB]
        return new BankAccount(dBankAccount.firstaccountno,
                dBankAccount.lastaccountno,
                dBankAccount.getFirstNumber(),
                dBankAccount.getLastNumber(),
                dBankAccount.bankcode);
    }

    @Override
    public void resume() {
        if (mView != null && mView.getUserVisibleHint()) {
            getMapBankAccount();
        }
    }

    private void onGetLinkedAccountSuccess(List<BankAccount> list) {
        hideLoadingView();
        mView.refreshLinkedAccount(list);
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
    void onAddCardSuccess(DBaseMap mappedCreditCard) {
        if (mView == null || !(mappedCreditCard instanceof DBankAccount)) {
            return;
        }

        String firstAccountNo = ((DBankAccount) mappedCreditCard).firstaccountno;
        String lastAccountNo = ((DBankAccount) mappedCreditCard).lastaccountno;
        BankAccount bankAccount = new BankAccount(firstAccountNo,
                lastAccountNo,
                mappedCreditCard.getFirstNumber(),
                mappedCreditCard.getLastNumber(),
                mappedCreditCard.bankcode);
        mView.insertData(bankAccount);

        if (mPayAfterLinkAcc) {
            mView.showConfirmPayAfterLinkAcc();
        }
    }

    @Override
    void onPayResponseError(PaymentError paymentError) {

    }

    @Override
    void onLoadIconFontSuccess() {
        if (mView != null) {
            mView.refreshLinkedAccount();
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
    void onUpdateVersion(boolean forceUpdate, String latestVersion, String message) {
        if (mView == null) {
            return;
        }
        mView.onUpdateVersion(forceUpdate, latestVersion, message);
    }

    @Override
    void onGetCardSupportSuccess(ArrayList<ZPCard> cardSupportList) {
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

    private void finishActivity() {
        if (mView != null && mView.getActivity() != null && !mView.getActivity().isFinishing()) {
            mView.getActivity().finish();
        }
    }

    private void showRetryGetLinkedAccount() {
        hideLoadingView();
        if (mView == null || mView.getContext() == null) {
            return;
        }
        mView.showRetryDialog(mView.getContext().getString(R.string.exception_get_linked_account_try_again),
                new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {
                        finishActivity();
                    }

                    @Override
                    public void onOKevent() {
                        getMapBankAccount();
                    }
                });
    }

    private class GetLinkedAccountSubscriber extends DefaultSubscriber<List<BankAccount>> {
        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                // simply ignore the error
                // because it is handled from event subscribers
                return;
            }

            Timber.e(e, "Get linked account throw exception.");
            showRetryGetLinkedAccount();
        }

        @Override
        public void onNext(List<BankAccount> bankAccounts) {
            /*ArrayList<BankAccount> tmp = new ArrayList<>();
            tmp.add(new BankAccount("A", "Nguyễn Văn", "213134", "1231", "123PSCB"));
            tmp.add(new BankAccount("B", "Nguyễn Văn", "123456", "4321", "ZPVCB"));
            tmp.add(new BankAccount("C", "Nguyễn Văn", "432100", "6789", "123PVTB"));*/
            onGetLinkedAccountSuccess(bankAccounts);
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

    private boolean isExist(List<BankAccount> bankAccounts, @NonNull ZPCard zpCard) {
        if (Lists.isEmptyOrNull(bankAccounts)) {
            return false;
        }
        for (int i = 0; i < bankAccounts.size(); i++) {
            BankAccount bankAccount = bankAccounts.get(i);
            if (bankAccount == null || TextUtils.isEmpty(bankAccount.mBankCode)) {
                continue;
            }
            if (bankAccount.mBankCode.equals(zpCard.getCardCode())) {
                return true;
            }
        }
        return false;
    }

    private class LinkAccountIfNotLinkedSubscriber extends DefaultSubscriber<List<BankAccount>> {
        private ZPCard mZPCard;

        LinkAccountIfNotLinkedSubscriber(@NonNull ZPCard zpCard) {
            mZPCard = zpCard;
        }

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                // simply ignore the error
                // because it is handled from event subscribers
                return;
            }

            Timber.e(e, "Get linked account before link throw exception.");
            linkAccount(mZPCard);
        }

        @Override
        public void onNext(List<BankAccount> bankAccounts) {
            if (isExist(bankAccounts, mZPCard)) {
                showAccountHasLinked(mZPCard);
            } else {
                linkAccount(mZPCard);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadIconFontSuccess(LoadIconFontEvent event) {
        Timber.d("Load icon font success.");
        if (event != null && mView != null) {
            mView.refreshLinkedAccount();
        }
    }
}

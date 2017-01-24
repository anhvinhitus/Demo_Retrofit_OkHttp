package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

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
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.merchant.CShareData;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;
import vn.com.zalopay.wallet.merchant.listener.IGetCardSupportListListener;

/**
 * Created by longlv on 1/17/17.
 * *
 */
class LinkAccountPresenter extends AbstractLinkCardPresenter<ILinkAccountView> {

    private IGetCardSupportListListener mGetCardSupportListListener;

    @Inject
    LinkAccountPresenter(ZaloPayRepository zaloPayRepository,
                         Navigator navigator,
                         BalanceStore.Repository balanceRepository,
                         TransactionStore.Repository transactionRepository,
                         User user,
                         SharedPreferences sharedPreferences, EventBus eventBus) {
        super(zaloPayRepository, navigator, balanceRepository, transactionRepository,
                user, sharedPreferences, eventBus);
        mGetCardSupportListListener = new IGetCardSupportListListener() {
            @Override
            public void onProcess() {
                Timber.d("getCardSupportList onProcess");
            }

            @Override
            public void onComplete(ArrayList<ZPCard> cardSupportList) {
                Timber.d("getCardSupportList onComplete cardSupportList[%s]", cardSupportList);
                hideLoadingView();
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
                    showErrorView("Chưa có ngân hàng hỗ trợ liên kết tài khoản.");
                } else  if (cards.size() == 1) {
                    linkAccount(cards.get(0));
                } else {
                    if (mView != null) {
                        mView.showListBankDialog(cards);
                    }
                }
            }

            @Override
            public void onError(String pErrorMess) {
                Timber.d("cardSupportHashMap onError [%s]", pErrorMess);
                hideLoadingView();
                showRetryDialog();
            }

            @Override
            public void onUpVersion(boolean forceUpdate, String latestVersion, String message) {
                hideLoadingView();
                if (mView == null) {
                    return;
                }
                mView.onEventUpdateVersion(forceUpdate, latestVersion, message);
            }
        };
    }

    void getMapBankAccount() {
        showLoadingView();
        Subscription subscription = ObservableHelper.makeObservable(new Callable<List<BankAccount>>() {
            @Override
            public List<BankAccount> call() throws Exception {
                List<DBankAccount> mapCardLis = CShareData.getInstance().getMapBankAccountList(mUser.zaloPayId);
                return transform(mapCardLis);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new GetLinkedAccountSubscriber());
        mSubscription.add(subscription);
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

    void showListBankSupportLinkAcc() {
        Timber.d("Show list bank that support link account.");
        showLoadingView();
        UserInfo userInfo = new UserInfo();
        userInfo.zaloPayUserId = mUser.zaloPayId;
        userInfo.accessToken = mUser.accesstoken;
        CShareData.getInstance().setUserInfo(userInfo).getCardSupportList(mGetCardSupportListListener);
    }

    private void onGetLinkedAccountSuccess(List<BankAccount> list) {
        hideLoadingView();
        mView.refreshLinkedAccount(list);
    }

    void linkAccount(ZPCard zpCard) {
        Timber.d("linkAccount card[%s]", zpCard);
        if (paymentWrapper == null || mView == null || zpCard == null) {
            return;
        }
        showLoadingView();
        paymentWrapper.linkAccount(mView.getActivity(), zpCard.getCardCode());
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
        if (mView == null) {
            return;
        }
        if (mappedCreditCard != null) {
            BankAccount bankAccount = new BankAccount("", "",
                    mappedCreditCard.getFirstNumber(),
                    mappedCreditCard.getLastNumber(),
                    mappedCreditCard.bankcode);
            mView.insertData(bankAccount);
        }
    }


    private void showRetryDialog() {
        if (mView == null) {
            return;
        }
        mView.showRetryDialog(mView.getContext().getString(R.string.exception_generic), new ZPWOnEventConfirmDialogListener() {
            @Override
            public void onCancelEvent() {

            }

            @Override
            public void onOKevent() {
                showListBankSupportLinkAcc();
            }
        });
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

    private class GetLinkedAccountSubscriber extends DefaultSubscriber<List<BankAccount>> {
        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                // simply ignore the error
                // because it is handled from event subscribers
                return;
            }

            Timber.e(e, "LinkAccountSubscriber ");
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
}

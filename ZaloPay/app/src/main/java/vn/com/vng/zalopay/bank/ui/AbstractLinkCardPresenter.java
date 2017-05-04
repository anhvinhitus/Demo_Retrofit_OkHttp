package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Subscription;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.models.BankAccount;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.BankCard;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.event.DownloadSDKResourceComplete;
import vn.com.vng.zalopay.event.LoadIconFontEvent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.DefaultPaymentResponseListener;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.service.PaymentWrapperBuilder;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.enumeration.ECardType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;

/**
 * Created by longlv on 10/25/16.
 * Common function of LinkCardPresenter & LinkAccountPresenter
 */

abstract class AbstractLinkCardPresenter<View> extends AbstractPresenter<View> {
    protected PaymentWrapper paymentWrapper;
    private Navigator mNavigator;
    boolean mPayAfterLinkBank;

    User mUser;

    protected EventBus mEventBus;

    abstract Activity getActivity();

    abstract Context getContext();

    abstract void onPreComplete();

    abstract void onResponseSuccessFromSDK(ZPPaymentResult zpPaymentResult);

    abstract void onLoadIconFontSuccess();

    abstract void onDownloadPaymentSDKComplete();

    abstract void showLoadingView();

    abstract void hideLoadingView();

    abstract void showErrorView(String message);

    abstract void showNetworkErrorDialog();

    abstract void showRetryDialog(String message, ZPWOnEventConfirmDialogListener listener);

    abstract void onGetCardSupportSuccess(List<ZPCard> cardSupportList);

    AbstractLinkCardPresenter(ZaloPayRepository zaloPayRepository,
                              Navigator navigator,
                              BalanceStore.Repository balanceRepository,
                              TransactionStore.Repository transactionRepository,
                              User user, EventBus eventBus) {
        mNavigator = navigator;
        this.mUser = user;
        this.mEventBus = eventBus;
        paymentWrapper = new PaymentWrapperBuilder()
                .setBalanceRepository(balanceRepository)
                .setZaloPayRepository(zaloPayRepository)
                .setTransactionRepository(transactionRepository)
                .setResponseListener(new PaymentResponseListener())
                .setLinkCardListener(new LinkCardListener(this))
                .build();
    }

    @Override
    public void attachView(View view) {
        super.attachView(view);
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    public void detachView() {
        mEventBus.unregister(this);
        super.detachView();
    }

    @Override
    public void destroy() {
        //release cache
        CShareDataWrapper.dispose();
        super.destroy();
    }

    void initData(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        mPayAfterLinkBank = bundle.getBoolean(Constants.ARG_CONTINUE_PAY_AFTER_LINK_Bank);
    }

    List<BankAccount> getLinkedBankAccount() {
        List<DBankAccount> mapCardLis = CShareDataWrapper.getMapBankAccountList(mUser.zaloPayId);
        return transformBankAccount(mapCardLis);
    }

    void getListBankSupport(DefaultSubscriber<List<ZPCard>> subscriber) {
        UserInfo userInfo = new UserInfo();
        userInfo.zaloPayUserId = mUser.zaloPayId;
        userInfo.accessToken = mUser.accesstoken;
        Subscription subscription = CShareDataWrapper.getCardSupportList(userInfo, subscriber);
        mSubscription.add(subscription);
    }

    void getListBankSupport() {
        Timber.d("Show list bank that support link account.");
        DefaultSubscriber subscriber = new DefaultSubscriber<List<ZPCard>>() {
            @Override
            public void onCompleted() {
                unsubscribe();
            }

            @Override
            public void onError(Throwable e) {
                unsubscribe();
                Timber.d("Get card support error : message [%s]", e.getMessage());
                hideLoadingView();
                if (getContext() != null) {
                    showRetryDialog(getContext().getString(R.string.exception_generic),
                            new ZPWOnEventConfirmDialogListener() {
                                @Override
                                public void onCancelEvent() {

                                }

                                @Override
                                public void onOKevent() {
                                    getListBankSupport();
                                }
                            });
                }
            }

            @Override
            public void onNext(List<ZPCard> cardList) {
                unsubscribe();
                Timber.d("Get card support onComplete : cardSupportList [%s]", cardList);
                hideLoadingView();
                onGetCardSupportSuccess(cardList);
            }
        };

        getListBankSupport(subscriber);
    }

    void addLinkAccount() {
        if (getContext() == null) {
            return;
        }
        if (mUser.profilelevel < 2) {
            mNavigator.startUpdateProfileLevel2Activity(getContext());
        } else {
            getListBankSupport();
        }
    }

    void addLinkCard() {
        if (getContext() == null) {
            return;
        }
        if (mUser.profilelevel < 2) {
            mNavigator.startUpdateProfileLevel2Activity(getContext());
        } else {
            paymentWrapper.linkCard(getActivity());
            hideLoadingView();
            ZPAnalytics.trackEvent(ZPEvents.MANAGECARD_ADDCARD_LAUNCH);
        }
    }

    void linkAccount(ZPCard zpCard) {
        if (zpCard == null) {
            return;
        }
        linkAccount(zpCard.getCardCode());
    }

    void linkAccount(String bankCode) {
        if (paymentWrapper == null || mView == null || TextUtils.isEmpty(bankCode)) {
            return;
        }
        Timber.d("Link account, card code [%s]", bankCode);
        paymentWrapper.linkAccount(getActivity(), bankCode);
        hideLoadingView();
    }

    boolean checkLinkedBankAccount(List<BankAccount> listBankAccount, String bankCode) {
        if (Lists.isEmptyOrNull(listBankAccount)) {
            return false;
        }
        for (BankAccount bankAccount : listBankAccount) {
            if (bankAccount == null || TextUtils.isEmpty(bankAccount.mBankCode)) {
                continue;
            }
            if (bankAccount.mBankCode.equalsIgnoreCase(bankCode)) {
                return true;
            }
        }
        return false;
    }

    private class PaymentResponseListener extends DefaultPaymentResponseListener {

        @Override
        protected ILoadDataView getView() {
            if (mView instanceof ILoadDataView) {
                return (ILoadDataView) mView;
            }
            return null;
        }

        @Override
        public void onParameterError(String param) {
            showErrorView(param);
        }

        @Override
        public void onResponseError(PaymentError paymentError) {
            if (paymentError == PaymentError.ERR_CODE_INTERNET) {
                showNetworkErrorDialog();
            }
        }

        @Override
        public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
            if (zpPaymentResult == null) {
                return;
            }
            onResponseSuccessFromSDK(zpPaymentResult);
        }

        @Override
        public void onAppError(String msg) {
            showErrorView(msg);
        }

        @Override
        public void onPreComplete(boolean isSuccessful, String tId, String pAppTransId) {
            Timber.d("onPreComplete : transactionId %s isSuccessful [%s] pAppTransId [%s]", tId, isSuccessful, pAppTransId);
            if (isSuccessful) {
                AbstractLinkCardPresenter.this.onPreComplete();
            }
        }
    }

    private static class LinkCardListener implements PaymentWrapper.ILinkCardListener {
        WeakReference<AbstractLinkCardPresenter> mWeakReference;

        LinkCardListener(AbstractLinkCardPresenter presenter) {
            mWeakReference = new WeakReference<>(presenter);
        }

        @Override
        public void onErrorLinkCardButInputBankAccount(DBaseMap bankInfo) {
            if (mWeakReference.get() == null) {
                return;
            }

            mWeakReference.get().onErrorLinkCardButInputBankAccount(bankInfo);
        }
    }

    protected void onErrorLinkCardButInputBankAccount(DBaseMap bankInfo) {
    }

    List<BankCard> transformBankCard(List<DMappedCard> cards) {
        if (Lists.isEmptyOrNull(cards)) return Collections.emptyList();

        List<BankCard> list = new ArrayList<>();

        for (DMappedCard dMappedCard : cards) {
            BankCard bCard = transformBankCard(dMappedCard);
            if (bCard != null) {
                list.add(bCard);
            }
        }

        return list;
    }

    private BankCard transformBankCard(DMappedCard card) {
        BankCard bankCard = null;

        if (card != null) {
            bankCard = new BankCard(card.cardname, card.first6cardno, card.last4cardno, card.bankcode);
            try {
                bankCard.type = detectCardType(card.bankcode, card.first6cardno);
                Timber.d("transform bankCard : type %s cardname %s first %s last %s", bankCard.type, card.cardname, card.first6cardno, card.last4cardno);
            } catch (Exception e) {
                Timber.d(e, "transform DMappedCard to BankCard exception [%s]", e.getMessage());
            }
        }

        return bankCard;
    }

    List<BankAccount> transformBankAccount(List<DBankAccount> bankAccounts) {
        if (Lists.isEmptyOrNull(bankAccounts)) return Collections.emptyList();

        List<BankAccount> list = new ArrayList<>();
        for (DBankAccount dBankAccount : bankAccounts) {
            BankAccount bankAccount = transformBankAccount(dBankAccount);
            if (bankAccount != null) {
                list.add(bankAccount);
            }
        }
        return list;
    }

    BankAccount transformBankAccount(DBankAccount dBankAccount) {
        if (dBankAccount == null) {
            return null;
        }

        //bankCode [ZPVCB] firstaccountno[012240] lastaccountno[2165]
        return new BankAccount(dBankAccount.firstaccountno,
                dBankAccount.lastaccountno,
                dBankAccount.bankcode);
    }


    String detectCardType(String bankcode, String first6cardno) {
        if (TextUtils.isEmpty(bankcode)) {
            return ECardType.UNDEFINE.toString();
        } else if (bankcode.equals(ECardType.PVTB.toString())) {
            return ECardType.PVTB.toString();
        } else if (bankcode.equals(ECardType.PBIDV.toString())) {
            return ECardType.PBIDV.toString();
        } else if (bankcode.equals(ECardType.PVCB.toString())) {
            return ECardType.PVCB.toString();
        } else if (bankcode.equals(ECardType.PSCB.toString())) {
            return ECardType.PSCB.toString();
        } else if (bankcode.equals(ECardType.PSGCB.toString())) {
            return ECardType.PSGCB.toString();
        /*} else if (bankcode.equals(ECardType.PEIB.toString())) {
            return ECardType.PEIB.toString();
        } else if (bankcode.equals(ECardType.PAGB.toString())) {
            return ECardType.PAGB.toString();
        } else if (bankcode.equals(ECardType.PTPB.toString())) {
            return ECardType.PTPB.toString();*/
        } else if (bankcode.equals(ECardType.UNDEFINE.toString())) {
            return ECardType.UNDEFINE.toString();
        } else {

            UserInfo userInfo = new UserInfo();
            userInfo.zaloPayUserId = mUser.zaloPayId;
            userInfo.accessToken = mUser.accesstoken;

            try {
                return CShareDataWrapper.detectCardType(userInfo, first6cardno);
            } catch (Exception e) {
                Timber.w(e, "detectCardType exception [%s]", e.getMessage());
            }
        }
        return ECardType.UNDEFINE.toString();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onLoadIconFontSuccess(LoadIconFontEvent event) {
        mEventBus.removeStickyEvent(LoadIconFontEvent.class);
        onLoadIconFontSuccess();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadSDKResourceComplete(DownloadSDKResourceComplete event) {
        onDownloadPaymentSDKComplete();
    }
}

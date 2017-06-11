package vn.com.vng.zalopay.ui.presenter;

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

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.bank.BankUtils;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.eventbus.ChangeBalanceEvent;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.zalosdk.ZaloSdkApi;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.PassportRepository;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.event.ZaloPayNameEvent;
import vn.com.vng.zalopay.event.ZaloProfileInfoEvent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.pw.DefaultPaymentResponseListener;
import vn.com.vng.zalopay.pw.PaymentWrapper;
import vn.com.vng.zalopay.pw.PaymentWrapperBuilder;
import vn.com.vng.zalopay.ui.activity.BaseActivity;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.vng.zalopay.ui.view.IPersonalView;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;

/**
 * Created by datnt10 on 3/27/17.
 * Handle actions, events, ui for tab Cá Nhân
 */

public class PersonalPresenter extends AbstractPresenter<IPersonalView> {
    private User mUser;
    private EventBus mEventBus;
    private BalanceStore.Repository mBalanceRepository;
    private PassportRepository mPassportRepository;
    private ZaloPayRepository mZaloPayRepository;
    private TransactionStore.Repository mTransactionRepository;
    private PaymentWrapper paymentWrapper;
    private ZaloSdkApi mZaloSdkApi;
    private Navigator mNavigator;
    private int linkBankStatus;

    public int getLinkBankStatus() {
        return linkBankStatus;
    }

    private void setLinkBankStatus(int linkBankStatus) {
        this.linkBankStatus = linkBankStatus;
    }

    @Inject
    PersonalPresenter(User user
            , EventBus eventBus
            , UserConfig userConfig
            , BalanceStore.Repository balanceRepository
            , PassportRepository passportRepository
            , ZaloPayRepository zaloPayRepository
            , TransactionStore.Repository transactionRepository
            , ZaloSdkApi zaloSdkApi
            , Navigator navigator) {
        this.mUser = user;
        this.mEventBus = eventBus;
        this.mBalanceRepository = balanceRepository;
        this.mPassportRepository = passportRepository;
        this.mZaloPayRepository = zaloPayRepository;
        this.mTransactionRepository = transactionRepository;
        this.mZaloSdkApi = zaloSdkApi;
        this.mNavigator = navigator;
        paymentWrapper = getPaymentWrapper();
        Timber.d("accesstoken[%s]", userConfig.getCurrentUser().accesstoken);
    }

    @Override
    public void attachView(IPersonalView iPersonalView) {
        super.attachView(iPersonalView);
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
    public void resume() {
        super.resume();
        initialize();
    }

    public Context getContext() {
        if (mView == null) {
            return null;
        }
        return mView.getContext();
    }

    public Activity getActivity() {
        if (mView == null) {
            return null;
        }
        return mView.getActivity();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEventMainThread(ZaloProfileInfoEvent event) {
        //UPDATE USERINFO
        mUser.avatar = event.avatar;
        mUser.displayName = event.displayName;

        if (mView != null) {
            mView.setAvatar(event.avatar);
            mView.setDisplayName(event.displayName);
        }

        mEventBus.removeStickyEvent(ZaloProfileInfoEvent.class);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onZaloPayNameEventMainThread(ZaloPayNameEvent event) {
        if (mView != null) {
            mView.setZaloPayName(event.zaloPayName);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onZaloPayUpdateBalanceMainThread(ChangeBalanceEvent event) {
        if (mView != null) {
            mView.setBalance(event.balance);
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNetworkChange(NetworkChangeEvent event) {
        if (!event.isOnline) {
            return;
        }

        if (TextUtils.isEmpty(mUser.displayName) ||
                TextUtils.isEmpty(mUser.avatar)) {
            mZaloSdkApi.getProfile();
        }
    }

    public void initialize() {
        mView.setUserInfo(mUser);
        mView.setBalance(mBalanceRepository.currentBalance());
        getBalanceLocal();
        checkLinkCardStatus();
    }

    private void getBalanceLocal() {
        Subscription subscription = mBalanceRepository.balanceLocal()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new PersonalPresenter.BalanceSubscriber());

        mSubscription.add(subscription);
    }

    private class BalanceSubscriber extends DefaultSubscriber<Long> {
        @Override
        public void onNext(Long aLong) {
            onGetBalanceSuccess(aLong);
        }
    }

    private void onGetBalanceSuccess(Long balance) {
        Timber.d("onGetBalanceSuccess %s", balance);
        mView.setBalance(balance);
    }

    public void logout() {
        Subscription subscription = mPassportRepository.logout()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>());
        mSubscription.add(subscription);

        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this);
        }

        if (mView == null) {
            return;
        }

        ((BaseActivity) mView.getContext()).clearUserSession(null);

    }

    private void checkLinkCardStatus() {
        List<MapCard> mapCardList = CShareDataWrapper.getMappedCardList(mUser);
        List<BankAccount> mapAccList = CShareDataWrapper.getMapBankAccountList(mUser);

        if (Lists.isEmptyOrNull(mapCardList) && Lists.isEmptyOrNull(mapAccList)) {
            // Chưa có liên kết thẻ, liên kết tài khoản
            setLinkBankStatus(Constants.LINK_BANK_NONE);
        } else if (!Lists.isEmptyOrNull(mapCardList) && Lists.isEmptyOrNull(mapAccList)) {
            // Đã liên kết thẻ, chưa liên kết tài khoản
            setLinkBankStatus(Constants.LINK_BANK_CARD_LINKED);
        } else if (Lists.isEmptyOrNull(mapCardList) && !Lists.isEmptyOrNull(mapAccList)) {
            // Chưa liên kết thẻ, đã liên kết tài khoản
            setLinkBankStatus(Constants.LINK_BANK_ACCOUNT_LINKED);
        } else {
            // Đã liên kết thẻ, liên kết tài khoản
            setLinkBankStatus(Constants.LINK_BANK_CARD_ACCOUNT_LINKED);
        }

        if (mView != null) {
            mView.setBankLinkText(getLinkBankStatus(), mapCardList.size(), mapAccList.size());
        }
    }

    public void addLinkCard() {
//        if (paymentWrapper == null) {
//            paymentWrapper = getPaymentWrapper();
//        }
//
//        paymentWrapper.linkCard(activity);

        mNavigator.startLinkCardActivityForResult(getActivity(), "123PVTB");
    }

    void linkAccount(String bankCode) {
        if (paymentWrapper == null || mView == null || TextUtils.isEmpty(bankCode)) {
            return;
        }
        Timber.d("Link account, card code [%s]", bankCode);
        paymentWrapper.linkAccount(getActivity(), bankCode);
    }

    private PaymentWrapper getPaymentWrapper() {
        PaymentWrapper wrapper = new PaymentWrapperBuilder()
                .setResponseListener(new DefaultPaymentResponseListener() {
                    @Override
                    protected ILoadDataView getView() {
                        return null;
                    }

                    @Override
                    public void onResponseError(PaymentError paymentError) {
                        if (paymentError == PaymentError.ZPC_TRANXSTATUS_NEED_LINK_ACCOUNT) {
                            // Go to LinkBankActivity with page index = 1 (Tab "Liên kết tài khoản")
                            mNavigator.startLinkAccountActivity(getActivity());
                        }
                    }
                })
                .setLinkCardListener(new LinkCardListener())
                .build();
        wrapper.initializeComponents();

        return wrapper;
    }

    private class LinkCardListener implements PaymentWrapper.ILinkCardListener {
        @Override
        public void onErrorLinkCardButInputBankAccount(BaseMap bankInfo) {
            handleErrorLinkCardButInputBankAccount(bankInfo);
        }
    }

    private void handleErrorLinkCardButInputBankAccount(BaseMap bankInfo) {
        if (bankInfo == null) {
            return;
        }
        Timber.d("Start LinkAccount with bank code [%s]", bankInfo.bankcode);
        List<vn.com.vng.zalopay.bank.models.BankAccount> bankAccounts = getLinkedBankAccount();

        if (checkLinkedBankAccount(bankAccounts, bankInfo.bankcode)) {
            String bankName = BankUtils.getBankName(bankInfo.bankcode);
            String message;
            if (!TextUtils.isEmpty(bankName)) {
//                message = String.format(getString(R.string.bank_account_has_linked),
//                        bankName);
            } else {
//                message = getString(R.string.bank_account_has_linked_this_bank);
            }
            if (mView != null) {
//                mView.gotoTabLinkAccAndShowDialog(message);
            }
        } else {
            linkAccount(bankInfo.bankcode);
        }
    }

    private boolean checkLinkedBankAccount(List<vn.com.vng.zalopay.bank.models.BankAccount> listBankAccount, String bankCode) {
        if (Lists.isEmptyOrNull(listBankAccount)) {
            return false;
        }
        for (vn.com.vng.zalopay.bank.models.BankAccount bankAccount : listBankAccount) {
            if (bankAccount == null || TextUtils.isEmpty(bankAccount.mBankCode)) {
                continue;
            }
            if (bankAccount.mBankCode.equalsIgnoreCase(bankCode)) {
                return true;
            }
        }
        return false;
    }

    private List<vn.com.vng.zalopay.bank.models.BankAccount> getLinkedBankAccount() {
        List<BankAccount> mapCardList = CShareDataWrapper.getMapBankAccountList(mUser);
        return transformBankAccount(mapCardList);
    }

    private List<vn.com.vng.zalopay.bank.models.BankAccount> transformBankAccount(List<BankAccount> bankAccounts) {
        if (Lists.isEmptyOrNull(bankAccounts)) return Collections.emptyList();

        List<vn.com.vng.zalopay.bank.models.BankAccount> list = new ArrayList<>();
        for (BankAccount dBankAccount : bankAccounts) {
            vn.com.vng.zalopay.bank.models.BankAccount bankAccount = transformBankAccount(dBankAccount);
            if (bankAccount != null) {
                list.add(bankAccount);
            }
        }
        return list;
    }

    private vn.com.vng.zalopay.bank.models.BankAccount transformBankAccount(BankAccount dBankAccount) {
        if (dBankAccount == null) {
            return null;
        }

        //bankCode [ZPVCB] firstaccountno[012240] lastaccountno[2165]
        return new vn.com.vng.zalopay.bank.models.BankAccount(dBankAccount.firstaccountno,
                dBankAccount.lastaccountno,
                dBankAccount.bankcode);
    }
}

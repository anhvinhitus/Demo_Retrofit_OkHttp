package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.eventbus.ChangeBalanceEvent;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.zalosdk.ZaloSdkApi;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.event.ZaloProfileInfoEvent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;
import vn.com.vng.zalopay.ui.view.IPersonalView;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.zalopay.wallet.entity.bank.BankAccount;
import vn.com.zalopay.wallet.entity.bank.MapCard;

/**
 * Created by datnt10 on 3/27/17.
 * Handle actions, events, ui for tab Cá Nhân
 */
public class PersonalPresenter extends AbstractPresenter<IPersonalView> {
    private User mUser;
    private EventBus mEventBus;
    private BalanceStore.Repository mBalanceRepository;
    private ZaloSdkApi mZaloSdkApi;
    private Navigator mNavigator;
    private AppResourceStore.Repository mAppResourceRepository;
    private int accounts;

    @Inject
    PersonalPresenter(User user
            , EventBus eventBus
            , BalanceStore.Repository balanceRepository
            , ZaloSdkApi zaloSdkApi
            , Navigator navigator, AppResourceStore.Repository appResourceRepository) {
        this.mUser = user;
        this.mEventBus = eventBus;
        this.mBalanceRepository = balanceRepository;
        this.mZaloSdkApi = zaloSdkApi;
        this.mNavigator = navigator;
        this.mAppResourceRepository = appResourceRepository;
    }

    public int getAccounts() {
        return accounts;
    }

    public void setAccounts(int accounts) {
        this.accounts = accounts;
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
        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this);
        }
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

    public Fragment getFragment() {
        if (mView == null) {
            return null;
        }
        return mView.getFragment();
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
        boolean hasVoucherApp = hasVoucherApp();
        mView.visibleVoucherAppList(false);
    }

    private boolean hasVoucherApp() {
        return PaymentAppConfig.getAppResource(BuildConfig.VOUCHER_APP_ID) != null;
    }

    private void getBalanceLocal() {
        Subscription subscription = mBalanceRepository.balanceLocal()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new PersonalPresenter.BalanceSubscriber());

        mSubscription.add(subscription);
    }

    void onGetBalanceSuccess(Long balance) {
        Timber.d("onGetBalanceSuccess %s", balance);
        mView.setBalance(balance);
    }

    private void checkLinkCardStatus() {
        List<MapCard> mapCardList = CShareDataWrapper.getMappedCardList(mUser);
        List<BankAccount> mapAccList = CShareDataWrapper.getMapBankAccountList(mUser);

        if (Lists.isEmptyOrNull(mapCardList) && Lists.isEmptyOrNull(mapAccList)) {
            // Chưa có liên kết thẻ, liên kết tài khoản
            setAccounts(0);
        } else if (!Lists.isEmptyOrNull(mapCardList) && Lists.isEmptyOrNull(mapAccList)) {
            // Đã liên kết thẻ, chưa liên kết tài khoản
            setAccounts(mapCardList.size());
        } else if (Lists.isEmptyOrNull(mapCardList) && !Lists.isEmptyOrNull(mapAccList)) {
            // Chưa liên kết thẻ, đã liên kết tài khoản
            setAccounts(mapAccList.size());
        } else {
            // Đã liên kết thẻ, liên kết tài khoản
            setAccounts(mapCardList.size() + mapAccList.size());
        }

        if (mView != null) {
            mView.setBankLinkText(getAccounts());
        }
    }

//    public void logout() {
//        Subscription subscription = mPassportRepository.logout()
//                .subscribeOn(Schedulers.io())
//                .subscribe(new DefaultSubscriber<>());
//        mSubscription.add(subscription);
//
//        if (mEventBus.isRegistered(this)) {
//            mEventBus.unregister(this);
//        }
//
//        if (mView == null) {
//            return;
//        }
//
//        ((UserBaseActivity) mView.getContext()).clearUserSession(null);
//
//    }

    public void addLinkCard() {
        mNavigator.startBankSupportSelectionActivityWithoutBank(getContext());
    }

    /**
     * Start list Voucher
     */
    public void startVoucherApp() {
        Subscription subscription = mNavigator.startVoucherApp(mView.getActivity());
        if (subscription != null) {
            mSubscription.add(subscription);
        }
    }

    private class BalanceSubscriber extends DefaultSubscriber<Long> {
        BalanceSubscriber() {
        }

        @Override
        public void onNext(Long aLong) {
            onGetBalanceSuccess(aLong);
        }
    }
}
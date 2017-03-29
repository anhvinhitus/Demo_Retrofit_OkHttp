package vn.com.vng.zalopay.ui.presenter;

import android.content.Context;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.eventbus.ChangeBalanceEvent;
import vn.com.vng.zalopay.data.zalosdk.ZaloSdkApi;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.PassportRepository;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.event.ZaloPayNameEvent;
import vn.com.vng.zalopay.event.ZaloProfileInfoEvent;
import vn.com.vng.zalopay.ui.activity.BaseActivity;
import vn.com.vng.zalopay.ui.view.IPersonalView;

/**
 * Created by Duke on 3/27/17.
 */

public class PersonalPresenter extends AbstractPresenter<IPersonalView> {
    private User mUser;
    private EventBus mEventBus;
    private UserConfig mUserConfig;
    private Context context;
    private BalanceStore.Repository mBalanceRepository;
    private PassportRepository mPassportRepository;

    @Inject
    ZaloSdkApi mZaloSdkApi;

    @Inject
    PersonalPresenter(User user
            , EventBus eventBus
            , UserConfig userConfig
            , BalanceStore.Repository balanceRepository
            , PassportRepository passportRepository
            , Context context) {
        this.mUser = user;
        this.mEventBus = eventBus;
        this.mUserConfig = userConfig;
        this.context = context;
        this.mBalanceRepository = balanceRepository;
        mPassportRepository = passportRepository;
        Timber.d("accessToken[%s]", userConfig.getCurrentUser().accesstoken);
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

}

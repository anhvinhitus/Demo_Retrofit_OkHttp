package vn.com.vng.zalopay.ui.presenter;

import android.content.Context;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.eventbus.ChangeBalanceEvent;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.data.zalosdk.ZaloSdkApi;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.PassportRepository;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.event.ZaloPayNameEvent;
import vn.com.vng.zalopay.event.ZaloProfileInfoEvent;
import vn.com.vng.zalopay.ui.activity.BaseActivity;
import vn.com.vng.zalopay.ui.view.IPersonalView;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;

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

    public int getLinkCardType() {
        return linkCardType;
    }

    public void setLinkCardType(int linkCardType) {
        this.linkCardType = linkCardType;
    }

    private int linkCardType;

    public void checkLinkCardStatus() {
        ObservableHelper.makeObservable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                List<DMappedCard> mapCardList = CShareDataWrapper.getMappedCardList(mUser.zaloPayId);
                List<DBankAccount> mapAccList = CShareDataWrapper.getMapBankAccountList(mUser.zaloPayId);
                if (Lists.isEmptyOrNull(mapCardList) && Lists.isEmptyOrNull(mapAccList)) {
                    // Chưa có liên kết thẻ, liên kết tài khoản
                    setLinkCardType(0);
                    return 0;
                } else if (!Lists.isEmptyOrNull(mapCardList)) {
                    // Đã liên kết thẻ, chưa liên kết tài khoản
                    return 1;
                } else if (!Lists.isEmptyOrNull(mapAccList)) {
                    // Chưa liên kết thẻ, đã liên kết tài khoản
                    return 2;
                } else {
                    // Đã liên kết thẻ, liên kết tài khoản
                    return 3;
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new PersonalPresenter.ChangeLinkBankTextSubscriber());
    }

    private class ChangeLinkBankTextSubscriber extends DefaultSubscriber<Integer> {

        @Override
        public void onNext(Integer textCode) {
            if (mView != null && textCode != null) {
                mView.setBankLinkText(textCode);
            }
        }

        @Override
        public void onError(Throwable e) {
            Timber.w(e, "Change page in context throw exception.");
            super.onError(e);
        }
    }

}

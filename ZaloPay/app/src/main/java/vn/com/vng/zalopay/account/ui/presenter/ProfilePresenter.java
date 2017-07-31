package vn.com.vng.zalopay.account.ui.presenter;

import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.view.IProfileView;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.PassportRepository;
import vn.com.vng.zalopay.event.ZaloPayNameEvent;
import vn.com.vng.zalopay.event.ZaloProfileInfoEvent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.user.UserBaseActivity;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by longlv on 25/05/2016.
 * *
 */
public class ProfilePresenter extends AbstractPresenter<IProfileView> {
    private EventBus mEventBus;
    private UserConfig mUserConfig;
    private User mUser;
    private AccountStore.Repository mAccountRepository;
    private PassportRepository mPassportRepository;
    private Navigator mNavigator;

    @Inject
    ProfilePresenter(EventBus eventBus,
                     UserConfig userConfig,
                     AccountStore.Repository accountRepository,
                     PassportRepository passportRepository,
                     Navigator navigator, User user) {
        this.mEventBus = eventBus;
        this.mUserConfig = userConfig;
        this.mAccountRepository = accountRepository;
        this.mPassportRepository = passportRepository;
        this.mNavigator = navigator;
        this.mUser = user;
    }

    @Override
    public void attachView(IProfileView iProfileView) {
        super.attachView(iProfileView);

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
        User user = mUser;
        if (user != null) {
            updateUserInfo(user);
        }
    }

    public void getProfile() {
        User user = mUser;
        if (user != null) {
            updateUserInfo(user);
            // Neu chua co ZaloPayName hoac Chua get profile level 3
            if (TextUtils.isEmpty(user.zalopayname) ||
                    (user.profilelevel > User.MIN_PROFILE_LEVEL && TextUtils.isEmpty(user.identityNumber))) {
                getUserProfile();
            }
        }
    }

    public void showLoading() {
        if (mView != null) {
            mView.showLoading();
        }
    }

    public void hideLoading() {
        if (mView != null) {
            mView.hideLoading();
        }
    }

    private void getUserProfile() {
        Subscription subscription = mAccountRepository.getUserProfileLevelCloud()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ProfileSubscriber());
        mSubscription.add(subscription);
    }

    void getProfileSuccess() {
        User user = mUser;
        if (user != null) {
            updateUserInfo(user);
        }
    }

    public void updateIdentity() {
        updateLevel3(true);
    }

    public void updateEmail() {
        updateLevel3(false);
    }

    private void updateLevel3(boolean isIdentity) {
        if (mView == null) {
            return;
        }

        if (mUserConfig.isWaitingApproveProfileLevel3()) {
            int message = isIdentity ? R.string.waiting_approve_identity : R.string.waiting_approve_email;
            mView.showNotificationDialog(mView.getContext().getString(message));
        } else {
            mNavigator.startUpdateProfile3Activity(mView.getContext(), isIdentity);
        }
    }

    public void updateZaloPayID() {
        if (!TextUtils.isEmpty(mUser.zalopayname)) {
            return;
        }
        if (mView == null) {
            return;
        }

        mNavigator.startEditAccountActivity(mView.getContext());
        ZPAnalytics.trackEvent(ZPEvents.ME_PROFILE_ZPID_LAUNCH);
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

        ((UserBaseActivity) mView.getActivity()).clearUserSession(null);

    }

    private class ProfileSubscriber extends DefaultSubscriber<Boolean> {

        ProfileSubscriber() {
        }

        @Override
        public void onCompleted() {
            ProfilePresenter.this.getProfileSuccess();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onZaloPayNameEventMainThread(ZaloPayNameEvent event) {
        if (mView != null) {
            mView.setZaloPayName(event.zaloPayName);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true, priority = 1)
    public void onEventMainThread(ZaloProfileInfoEvent event) {
        Timber.d("onEventMainThread event %s", event);
        updateUserInfo(mUser);
    }

    private void updateUserInfo(User user) {
        if (mView != null) {
            mView.updateUserInfo(user);
        }
    }

    public boolean isWaitingApproveProfileLevel3() {
        return mUserConfig.isWaitingApproveProfileLevel3();
    }
}

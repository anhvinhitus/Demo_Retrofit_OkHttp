package vn.com.vng.zalopay.account.ui.presenter;

import android.text.TextUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.account.ui.view.IProfileView;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.interactor.event.ZaloPayNameEvent;
import vn.com.vng.zalopay.interactor.event.ZaloProfileInfoEvent;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by longlv on 25/05/2016.
 */
public class ProfilePresenter extends BaseUserPresenter implements IPresenter<IProfileView> {

    IProfileView mView;
    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public ProfilePresenter() {
    }

    @Override
    public void setView(IProfileView iProfileView) {
        mView = iProfileView;
        try {
            eventBus.register(this);
        } catch (Exception e) {
            Timber.d(e, "exception");
        }
    }

    @Override
    public void destroyView() {
        unsubscribeIfNotNull(compositeSubscription);
        mView = null;
        eventBus.unregister(this);
    }

    @Override
    public void resume() {
        getProfile();
        checkShowOrHideChangePinView();
    }

    @Override
    public void pause() {
    }

    @Override
    public void destroy() {
    }

    public void getProfile() {
        User user = userConfig.getCurrentUser();
        if (user != null) {
            mView.updateUserInfo(user);

            // Neu chua co ZaloPayName hoac Chua get profile level 3
            if (TextUtils.isEmpty(user.zalopayname) ||
                    (user.profilelevel >= 3 && TextUtils.isEmpty(user.identityNumber))) {
                getUserProfile();
            }
        }
    }

    private void checkShowOrHideChangePinView() {
        try {
            boolean isShow = userConfig.getCurrentUser().profilelevel < 2;
            mView.showHideChangePinView(isShow);
        } catch (Exception e) {
            Timber.d(e, "checkShowOrHideChangePinView");
        }
    }

    public int getProfileLevel() {
        User user = userConfig.getCurrentUser();
        if (user == null) {
            return 0;
        } else if (userConfig.getCurrentUser() == null) {
            return 0;
        } else {
            return userConfig.getCurrentUser().profilelevel;
        }
    }

    public void showLoading() {
        mView.showLoading();
    }

    public void hideLoading() {
        mView.hideLoading();
    }

    public void showRetry() {
        mView.showRetry();
    }

    public void hideRetry() {
        mView.hideRetry();
    }


    private void getUserProfile() {
        Subscription subscription = accountRepository.getUserProfileLevelCloud()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ProfileSubscriber());
        compositeSubscription.add(subscription);
    }

    private void getProfileSuccess() {
        User user = userConfig.getCurrentUser();
        if (user != null) {
            mView.updateUserInfo(user);
        }
    }

    private class ProfileSubscriber extends DefaultSubscriber<Boolean> {

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
        if (mView != null) {
            mView.updateUserInfo(userConfig.getCurrentUser());
        }
    }

}

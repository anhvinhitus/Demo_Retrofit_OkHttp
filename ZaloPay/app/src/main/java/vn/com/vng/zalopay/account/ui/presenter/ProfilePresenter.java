package vn.com.vng.zalopay.account.ui.presenter;

import android.text.TextUtils;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.vng.zalopay.account.ui.view.IProfileView;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
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
    }

    @Override
    public void destroyView() {
        unsubscribeIfNotNull(compositeSubscription);
        mView = null;
    }

    @Override
    public void resume() {
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
            if (user.profilelevel >= 3 && TextUtils.isEmpty(user.identityNumber)) { // Chua get profile level 3
                getUserProfile();
            }
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

    private final void getProfileSuccess() {
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

        @Override
        public void onError(Throwable e) {
        }
    }
}

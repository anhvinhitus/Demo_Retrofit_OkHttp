package vn.com.vng.zalopay.withdraw.ui.presenter;

import android.text.TextUtils;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.vng.zalopay.withdraw.ui.view.IWithdrawConditionView;
import vn.com.zalopay.wallet.data.GlobalData;

/**
 * Created by longlv on 11/08/2016.
 * Presenter of WithdrawConditionFragment
 */
public class WithdrawConditionPresenter extends BaseUserPresenter implements IPresenter<IWithdrawConditionView> {

    private IWithdrawConditionView mView;

    CompositeSubscription compositeSubscription = new CompositeSubscription();

    public void getProfile() {
        User user = userConfig.getCurrentUser();
        if (user != null) {
            mView.updateUserInfo(user);
            if (user.profilelevel >= 3 &&
                    (TextUtils.isEmpty(user.identityNumber) || TextUtils.isEmpty(user.email))) {
                getUserProfile();
            }
        }
    }

    private void getUserProfile() {
        Subscription subscription = accountRepository.getUserProfileLevelCloud()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ProfileSubscriber());
        compositeSubscription.add(subscription);
    }

    private class ProfileSubscriber extends DefaultSubscriber<Boolean> {

        @Override
        public void onCompleted() {
            WithdrawConditionPresenter.this.getProfileSuccess();
        }

        @Override
        public void onError(Throwable e) {
        }
    }

    private void getProfileSuccess() {
        User user = userConfig.getCurrentUser();
        if (user != null) {
            mView.updateUserInfo(user);
        }
    }


    @Override
    public void setView(IWithdrawConditionView view) {
        mView = view;
    }

    @Override
    public void destroyView() {
        unsubscribeIfNotNull(compositeSubscription);
        mView = null;
    }

    @Override
    public void resume() {
        getProfile();
    }

    @Override
    public void pause() {
    }

    @Override
    public void destroy() {
        GlobalData.initApplication(null);
    }
}

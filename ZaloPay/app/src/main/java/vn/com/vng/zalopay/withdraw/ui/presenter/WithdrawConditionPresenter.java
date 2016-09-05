package vn.com.vng.zalopay.withdraw.ui.presenter;

import android.app.Activity;
import android.text.TextUtils;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.vng.zalopay.withdraw.ui.view.IWithdrawConditionView;
import vn.com.zalopay.wallet.data.GlobalData;
import vn.com.zalopay.wallet.entity.enumeration.ECardType;

/**
 * Created by longlv on 11/08/2016.
 * Presenter of WithdrawConditionFragment
 */
public class WithdrawConditionPresenter extends AbsWithdrawConditionPresenter
        implements IPresenter<IWithdrawConditionView> {

    private IWithdrawConditionView mView;

    CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Inject
    public WithdrawConditionPresenter() {
    }

    private void getUserProfileFromServer() {
        Subscription subscription = accountRepository.getUserProfileLevelCloud()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ProfileSubscriber());
        compositeSubscription.add(subscription);
    }

    private class ProfileSubscriber extends DefaultSubscriber<Boolean> {

        @Override
        public void onCompleted() {
            WithdrawConditionPresenter.this.checkConditionAndStartWithdrawActivity();
        }

        @Override
        public void onError(Throwable e) {
        }
    }

    public boolean isValidCondition() {
        boolean isValidProfile = isValidProfileLevel();
        boolean isValidLinkCard = isValidLinkCard();
        boolean isValidCondition = isValidProfile && isValidLinkCard;
        if (isValidProfile) {
            mView.hideUpdateProfile();
            mView.hideUserNote();
        } else {
            if (userConfig.isWaitingApproveProfileLevel3()) {
                mView.hideUpdateProfile();
                mView.showUserNote();
            } else {
                mView.showUpdateProfile();
                mView.hideUserNote();

            }
            User user = userConfig.getCurrentUser();
            if (user.profilelevel >= 3 &&
                    (TextUtils.isEmpty(user.identityNumber) || TextUtils.isEmpty(user.email))) {
                getUserProfileFromServer();
            }
        }

        return isValidCondition;
    }

    private void checkConditionAndStartWithdrawActivity() {
        if (mView == null) {
            return;
        }
        if (isValidCondition()) {
            navigator.startWithdrawActivity(mView.getContext());
            mView.getActivity().finish();
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
        checkConditionAndStartWithdrawActivity();
    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {
        GlobalData.initApplication(null);
    }
    @Override
    public Activity getActivity() {
        if (mView == null) {
            return null;
        }
        return mView.getActivity();
    }

    @Override
    public void setChkEmail(boolean isValid) {
        if (mView == null) {
            return;
        }
        mView.setChkEmail(isValid);
    }

    @Override
    public void setChkIdentityNumber(boolean isValid) {
        if (mView == null) {
            return;
        }
        mView.setChkIdentityNumber(isValid);
    }

    @Override
    public void setBankValid(String bankCode, boolean isValid) {
        if (mView == null) {
            return;
        }
        if (ECardType.PVTB.toString().equals(bankCode)) {
            mView.setChkVietinBank(isValid);
        } else if (ECardType.PSCB.toString().equals(bankCode)) {
            mView.setChkSacomBank(isValid);
        }
    }

}

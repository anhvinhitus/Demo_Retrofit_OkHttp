package vn.com.vng.zalopay.account.ui.presenter;

import android.text.TextUtils;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.account.ui.view.IPinProfileView;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.ProfileLevel2;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by longlv on 25/05/2016.
 * *
 */
public class PinProfilePresenter extends BaseUserPresenter implements IPresenter<IPinProfileView> {

    private IPinProfileView mView;

    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    private AccountStore.Repository mAccountRepository;
    private User mUser;

    @Inject
    PinProfilePresenter(AccountStore.Repository accountRepository, User user) {
        this.mAccountRepository = accountRepository;
        mUser = user;
    }

    @Override
    public void setView(IPinProfileView iProfileView) {
        mView = iProfileView;
    }

    @Override
    public void destroyView() {
        hideLoading();
        this.mView = null;
    }

    private void unsubscribe() {
        unsubscribeIfNotNull(mCompositeSubscription);
    }

    @Override
    public void resume() {
        if (mUser != null && !TextUtils.isEmpty(mUser.zalopayname)) {
            mView.hideInputZaloPayName();
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {
        this.unsubscribe();
    }

    public void updateProfile(String pin, String phone, String zalopayName) {
        showLoading();
        Subscription subscriptionLogin = mAccountRepository.updateUserProfileLevel2(pin, phone, zalopayName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UpdateProfileSubscriber(phone, zalopayName));
        mCompositeSubscription.add(subscriptionLogin);
    }

    public void checkZaloPayName(String zaloPayName) {
        if (TextUtils.isEmpty(zaloPayName)) {
            return;
        }
        Subscription subscription = mAccountRepository.checkZaloPayNameExist(zaloPayName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new GetUserInfoByZPNameSubscriber());
        mCompositeSubscription.add(subscription);
    }

    private final class UpdateProfileSubscriber extends DefaultSubscriber<Boolean> {
        private String phone;
        private String zalopayName;

        UpdateProfileSubscriber(String phone, String zalopayName) {
            this.phone = phone;
            this.zalopayName = zalopayName;
        }

        @Override
        public void onNext(Boolean result) {
            Timber.d("updateProfile success " + result);
            PinProfilePresenter.this.onUpdateProfileSuccess(phone, zalopayName);
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }

            Timber.e(e, "update Profile Subscriber onError [%s]", e.getMessage());
            if (e instanceof BodyException) {
                if (((BodyException) e).errorCode == NetworkError.USER_EXISTED) {
                    ZPAnalytics.trackEvent(ZPEvents.UPDATEPROFILE2_ZPN_INUSED);
                    mView.onCheckFail();
                } else {
                    PinProfilePresenter.this.onUpdateProfileError(e.getMessage());
                }
            } else {
                PinProfilePresenter.this.onUpdateProfileError("Cập nhật thông tin người dùng thất bại.");
            }
        }
    }

    private void onUpdateProfileError(String error) {
        hideLoading();
        mView.showError(error);
    }

    private void onUpdateProfileSuccess(String phone, String zalopayName) {
        hideLoading();
        mView.updateProfileSuccess(phone, zalopayName);
        saveProfileInfo2Cache(phone, zalopayName, true);
    }

    public void saveProfileInfo2Cache(final String phone, final String zalopayName) {
        mAccountRepository.getProfileLevel2Cache()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<ProfileLevel2>() {
                    @Override
                    public void onNext(ProfileLevel2 profileLevel2) {
                        ProfileLevel2 newProfile = new ProfileLevel2();
                        newProfile.zaloPayName = zalopayName;
                        newProfile.phoneNumber = phone;
                        upgradeProfileInfo2Cache(profileLevel2, newProfile);
                    }
                });
    }

    private void upgradeProfileInfo2Cache(ProfileLevel2 profileLevel2, ProfileLevel2 newProfile) {
        String phone = newProfile.phoneNumber;
        String zalopayName = newProfile.zaloPayName;
        //If db haven't profileLevel2 then save profileLevel2
        if (profileLevel2 == null) {
            saveProfileInfo2Cache(phone, zalopayName, false);
        } else {
            //If phone || zalopayName is null or empty then haven't otp
            if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(zalopayName)) {
                saveProfileInfo2Cache(phone, zalopayName, false);
                return;
            }
            //update data if phone | phoneNumber don't equal data in db
            if (!phone.equals(profileLevel2.phoneNumber) ||
                    !zalopayName.equals(profileLevel2.zaloPayName)) {
                saveProfileInfo2Cache(phone, zalopayName, false);
            }
        }
    }

    private void saveProfileInfo2Cache(String phone, String zalopayName, boolean receiveOtp) {
        mAccountRepository.saveProfileInfo2(phone, zalopayName, receiveOtp);
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

    private class GetUserInfoByZPNameSubscriber extends DefaultSubscriber<Boolean> {

        @Override
        public void onError(Throwable e) {
            super.onError(e);
            if (e instanceof BodyException) {
                if (((BodyException) e).errorCode == NetworkError.USER_EXISTED) {
                    ZPAnalytics.trackEvent(ZPEvents.UPDATEPROFILE2_ZPN_INUSED2);
                }

                mView.onCheckFail();
            } else {
                mView.showError("Lỗi xảy ra trong quá trình kiểm tra tên tài khoản Zalo Pay.\nVui lòng thử lại.");
            }
        }

        @Override
        public void onNext(Boolean isValid) {
            if (isValid) {
                mView.onCheckSuccess();
            } else {
                mView.onCheckFail();
            }
        }
    }
}

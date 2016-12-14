package vn.com.vng.zalopay.account.ui.presenter;

import android.text.TextUtils;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.account.ui.view.IPinProfileView;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.ProfileLevel2;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;

/**
 * Created by longlv on 25/05/2016.
 * *
 */
public class PinProfilePresenter extends AbstractPresenter<IPinProfileView> {
    private AccountStore.Repository mAccountRepository;

    @Inject
    PinProfilePresenter(AccountStore.Repository accountRepository) {
        this.mAccountRepository = accountRepository;
    }

    @Override
    public void attachView(IPinProfileView iProfileView) {
        super.attachView(iProfileView);
        showProfileLevel2Cache();
    }

    @Override
    public void detachView() {
        hideLoading();
        super.detachView();
    }

    public void updateProfile(String pin, String phone) {
        showLoading();
        Subscription subscriptionLogin = mAccountRepository.updateUserProfileLevel2(pin, phone)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UpdateProfileSubscriber(phone));
        mSubscription.add(subscriptionLogin);
    }

    private final class UpdateProfileSubscriber extends DefaultSubscriber<Boolean> {
        private String phone;

        UpdateProfileSubscriber(String phone) {
            this.phone = phone;
        }

        @Override
        public void onNext(Boolean result) {
            Timber.d("updateProfile success " + result);
            PinProfilePresenter.this.onUpdateProfileSuccess(phone);
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
                PinProfilePresenter.this.onUpdateProfileError(e.getMessage());
            } else {
                PinProfilePresenter.this.onUpdateProfileError("Cập nhật thông tin người dùng thất bại.");
            }
        }
    }

    private void onUpdateProfileError(String error) {
        hideLoading();
        mView.showError(error);
    }

    private void onUpdateProfileSuccess(String phone) {
        hideLoading();
        mView.updateProfileSuccess(phone);
        saveProfileInfo2Cache(phone, true);
    }

    private void showProfileLevel2Cache() {
        mAccountRepository.getProfileLevel2Cache()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<ProfileLevel2>() {
                    @Override
                    public void onNext(ProfileLevel2 profileLevel2) {
                        if (profileLevel2 == null) {
                            return;
                        }
                        Timber.d("showProfileLevel2Cache phone [%s]", profileLevel2.phoneNumber);
                        Timber.d("showProfileLevel2Cache isReceivedOtp [%s]", profileLevel2.isReceivedOtp);
                        if (!TextUtils.isEmpty(profileLevel2.phoneNumber)) {
                            mView.setPhoneNumber(profileLevel2.phoneNumber);
                        }
                    }
                });
    }

    public void saveProfileInfo2Cache(final String phone) {
        mAccountRepository.getProfileLevel2Cache()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<ProfileLevel2>() {
                    @Override
                    public void onNext(ProfileLevel2 profileLevel2) {
                        ProfileLevel2 newProfile = new ProfileLevel2();
                        newProfile.phoneNumber = phone;
                        upgradeProfileInfo2Cache(profileLevel2, newProfile);
                    }
                });
    }

    private void upgradeProfileInfo2Cache(ProfileLevel2 profileLevel2, ProfileLevel2 newProfile) {
        String phone = newProfile.phoneNumber;
        //If db haven't profileLevel2 then save profileLevel2
        if (profileLevel2 == null) {
            saveProfileInfo2Cache(phone, false);
        } else {
            //update data if phone | phoneNumber don't equal data in db
            if (!profileLevel2.equals(newProfile)) {
                saveProfileInfo2Cache(phone, false);
            }
        }
    }

    private void saveProfileInfo2Cache(String phone, boolean receiveOtp) {
        Timber.d("saveProfileInfo2Cache phone [%s] receiveOtp [%s]",
                phone, receiveOtp);
        mAccountRepository.saveProfileInfo2(phone, receiveOtp)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<Void>());
    }

    public void showLoading() {
        if (mView == null) {
            return;
        }
        mView.showLoading();
    }

    public void hideLoading() {
        if (mView == null) {
            return;
        }
        mView.hideLoading();
    }
}

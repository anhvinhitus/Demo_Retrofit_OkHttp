package vn.com.vng.zalopay.account.ui.presenter;

import android.text.TextUtils;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.account.ui.view.IPreProfileView;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.ProfileLevel2;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;

/**
 * Created by longlv on 19/05/2016.
 * *
 */
public class PreProfilePresenter extends AbstractPresenter<IPreProfileView> {
    private AccountStore.Repository mAccountRepository;

    @Inject
    UserConfig mUserConfig;

    @Inject
    public PreProfilePresenter(AccountStore.Repository accountRepository) {
        this.mAccountRepository = accountRepository;
    }

    @Override
    public void attachView(IPreProfileView iPreProfileView) {
        super.attachView(iPreProfileView);
        initPagerContent();
    }

    private void initPagerContent() {
        Subscription subscription = mAccountRepository.getProfileLevel2Cache()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<ProfileLevel2>() {
                    @Override
                    public void onNext(ProfileLevel2 profileLevel2) {
                        if (profileLevel2 == null) {
                            mView.initPagerContent(0);
                            return;
                        }
                        Timber.d("initPagerContent phone [%s]", profileLevel2.phoneNumber);
                        Timber.d("initPagerContent isReceivedOtp [%s]", profileLevel2.isReceivedOtp);
                        mView.updateCurrentPhone(profileLevel2.phoneNumber);
                        if (!TextUtils.isEmpty(profileLevel2.phoneNumber)
                                && profileLevel2.isReceivedOtp) {
                            mView.initPagerContent(1);
                        } else {
                            mView.initPagerContent(0);
                        }
                    }
                });
        mSubscription.add(subscription);
    }

    public void saveUserPhone(String phone) {
        mUserConfig.updateUserPhone(phone);
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
}

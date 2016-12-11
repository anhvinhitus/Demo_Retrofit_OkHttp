package vn.com.vng.zalopay.ui.presenter;

import android.content.Context;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.PassportRepository;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.view.IInvitationCodeView;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by AnhHieu on 6/27/16.
 * *
 */
@Singleton
public class InvitationCodePresenter extends AbstractPresenter<IInvitationCodeView> {

    @Inject
    public InvitationCodePresenter(Context applicationContext, PassportRepository passportRepository) {
        this.mApplicationContext = applicationContext;
        this.mPassportRepository = passportRepository;
    }

    private Context mApplicationContext;
    private PassportRepository mPassportRepository;

    private void showLoadingView() {
        if (mView != null) {
            mView.showLoading();
        }
    }

    private void hideLoadingView() {
        if (mView != null) {
            mView.hideLoading();
        }
    }

    public void sendCode(String code) {
        showLoadingView();
        ZPAnalytics.trackEvent(ZPEvents.INPUTINVITATIONCODE);
        Subscription subscription = mPassportRepository.verifyCode(code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LoginPaymentSubscriber());
        mSubscription.add(subscription);
    }

    private final class LoginPaymentSubscriber extends DefaultSubscriber<User> {

        @Override
        public void onNext(User user) {
            Timber.d("login success " + user);
            // TODO: Use your own attributes to track content views in your app
            Answers.getInstance().logLogin(new LoginEvent().putSuccess(true));
            InvitationCodePresenter.this.onInvitationCodeSuccess(user);
        }

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                // simply ignore the error
                // because it is handled from event subscribers
                return;
            }
            InvitationCodePresenter.this.onInvitationCodeError(e);
        }
    }

    private void gotoHomeScreen() {
        ZPAnalytics.trackEvent(ZPEvents.APPLAUNCHHOMEFROMINVITATION);
        mView.gotoMainActivity();
    }

    private void onInvitationCodeSuccess(User user) {
        this.hideLoadingView();
        ZPAnalytics.trackEvent(ZPEvents.INVITATIONCODESUCCESS);
        if (AndroidApplication.instance().getUserComponent() == null) {
            AndroidApplication.instance().createUserComponent(user);
        }

        this.gotoHomeScreen();
    }

    private void onInvitationCodeError(Throwable e) {
        if (mView == null) {
            return;
        }

        hideLoadingView();
        if (e instanceof BodyException) {
            ZPAnalytics.trackEvent(ZPEvents.INVITATIONCODEWRONG);
            if (((BodyException) e).errorCode == NetworkError.INVITATION_CODE_INVALID) {
                mView.showLabelError();
                return;
            }
        }

        String message = ErrorMessageFactory.create(mApplicationContext, e);
        mView.showError(message);
    }
}

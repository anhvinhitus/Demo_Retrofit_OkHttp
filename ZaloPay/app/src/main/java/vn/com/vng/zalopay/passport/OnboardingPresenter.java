package vn.com.vng.zalopay.passport;

import android.content.Context;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.ServerErrorMessage;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.exception.VerifyTimeoutException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.PassportRepository;
import vn.com.vng.zalopay.event.ReceiveSmsEvent;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by hieuvm on 6/10/17.
 * *
 */
class OnboardingPresenter extends AbstractPresenter<IOnboardingView> {


    private PassportRepository mPassportRepository;
    private EventBus mEventBus;
    private Context mApplicationContext;

    @Inject
    OnboardingPresenter(PassportRepository passportRepository, EventBus eventBus, Context applicationContext) {
        mPassportRepository = passportRepository;
        mEventBus = eventBus;
        mApplicationContext = applicationContext;
    }

    public void resume() {
        super.resume();
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    public void pause() {
        super.pause();
        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this);
        }
    }

    void register(Long zaloid, String oauthcode, String password, String phone, boolean resend) {
        Timber.d("register zaloid %s password %s phone %s", zaloid, password, phone);

        Subscription subs = mPassportRepository.registerPhoneNumber(zaloid, oauthcode, password, phone)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RegisterSubscriber(resend));
        mSubscription.add(subs);
    }

    void authenticate(Long zaloid, String oauthcode, String otp) {

        Timber.d("authenticate zaloid %s otp %s ", zaloid, otp);

        Subscription subs = mPassportRepository.authenticatePhoneNumber(zaloid, oauthcode, otp)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new AuthenticateSubscriber());
        mSubscription.add(subs);
    }

    private void verifyTimeout() {
        if (mView == null) {
            return;
        }

        mView.gotoLoginPage();
        mView.finish();
    }

    private void onRegisterError(Throwable e) {
        hideLoadingView();
        if (ResponseHelper.shouldIgnoreError(e)) {
            return;
        }

        if (e instanceof VerifyTimeoutException) {
            verifyTimeout();
            return;
        }

        String msg = ErrorMessageFactory.create(mApplicationContext, e);
        boolean incorrectPhone = e instanceof BodyException && ((BodyException) e).errorCode == ServerErrorMessage.PHONE_EXIST;
        showRegisterError(incorrectPhone, msg);
    }

    private void onRegisterSuccess(Boolean t, boolean isResend) {
        hideLoadingView();

        if (mView == null) {
            return;
        }

        mView.nextPage();
        mView.startOTPCountDown();

        if (isResend) {
            mView.resendOTPSuccess();
        }
    }

    private class RegisterSubscriber extends DefaultSubscriber<Boolean> {
        private boolean isResend;

        RegisterSubscriber(boolean resend) {
            isResend = resend;
        }

        public void onStart() {
            showLoadingView();
        }

        public void onError(Throwable e) {
            onRegisterError(e);
        }

        public void onNext(Boolean t) {
            onRegisterSuccess(t, isResend);
        }
    }

    private class AuthenticateSubscriber extends DefaultSubscriber<User> {

        public void onStart() {
            showLoadingView();
        }

        public void onError(Throwable e) {
            onAuthenticationError(e);
        }

        public void onNext(User user) {
            onAuthenticated(user);
        }
    }

    private void onAuthenticated(User user) {
        Answers.getInstance().logLogin(new LoginEvent().putSuccess(true));
        ZPAnalytics.trackEvent(ZPEvents.APPLAUNCHHOMEFROMLOGIN);
        AndroidApplication.instance().createUserComponent(user);

        if (mView == null) {
            return;
        }

        hideLoadingView();
        mView.showIncorrectOtp("");
        mView.gotoHomePage();
        mView.finish();

    }

    private void onAuthenticationError(Throwable e) {
        hideLoadingView();

        if (ResponseHelper.shouldIgnoreError(e)) {
            return;
        }

        if (e instanceof VerifyTimeoutException) {
            verifyTimeout();
            return;
        }

        String msg = ErrorMessageFactory.create(mApplicationContext, e);
        boolean incorrectOtp = e instanceof BodyException && ((BodyException) e).errorCode == ServerErrorMessage.INCORRECT_OTP;
        showAuthenticationError(incorrectOtp, msg);
    }

    private void showAuthenticationError(boolean incorrectOtp, String msg) {
        if (mView == null) {
            return;
        }

        if (incorrectOtp) {
            mView.showIncorrectOtp(msg);
        } else {
            mView.showError(msg);
        }
    }

    private void showRegisterError(boolean incorrectPhone, String msg) {
        if (mView == null) {
            return;
        }

        if (incorrectPhone) {
            mView.showIncorrectPhone(msg);
        } else {
            mView.showError(msg);
        }
    }

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveSmsMessages(ReceiveSmsEvent event) {

        String pattern = "(.*)(\\d{6})(.*)";
        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);
        for (ReceiveSmsEvent.SmsMessage message : event.messages) {
            Timber.d("Receive SMS: [%s: %s]", message.from, message.body);
            Matcher m = r.matcher(message.body);
            if (m.find()) {
                Timber.d("Found OTP: %s", m.group(2));
                if (mView != null) {
                    mView.setOtp(m.group(2));
                }
            }
        }
    }
}
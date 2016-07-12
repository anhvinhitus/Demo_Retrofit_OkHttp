package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;
import com.zing.zalo.zalosdk.oauth.LoginVia;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.network.listener.LoginListener;
import vn.com.vng.zalopay.analytics.ZPEvents;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.exception.InvitationCodeException;
import vn.com.vng.zalopay.data.exception.ServerMaintainException;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.view.ILoginView;

/**
 * Created by AnhHieu on 3/26/16.
 */

@Singleton
public final class LoginPresenter extends BaseAppPresenter implements IPresenter<ILoginView>, LoginListener.ILoginZaloListener {

    private ILoginView mView;

    private Subscription subscriptionLogin;

    @Inject
    public LoginPresenter() {
    }

    @Override
    public void setView(ILoginView view) {
        this.mView = view;
        Timber.d("setView: mview %s", mView);
    }

    @Override
    public void destroyView() {
        hideLoadingView();

        this.mView = null;
        Timber.d("destroyView:");
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void destroy() {
        this.destroyView();
        this.unsubscribe();
    }

    private void unsubscribe() {
        unsubscribeIfNotNull(subscriptionLogin);
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        try {
            ZaloSDK.Instance.onActivityResult(activity, requestCode, resultCode, data);
        } catch (Exception ex) {
            Timber.w(ex, " message " + ex.getMessage());
        }
    }

    public void loginZalo(Activity activity) {
        if (NetworkHelper.isNetworkAvailable(mView.getContext())) {
            showLoadingView();
            ZaloSDK.Instance.authenticate(activity, LoginVia.APP_OR_WEB, new LoginListener(this));
        } else {
            showErrorView(applicationContext.getString(R.string.exception_no_connection_try_again));
        }
    }

    @Override
    public void onAuthError(int errorCode, String message) {

     /*   zaloProfilePreferences.setUserId(0);
        zaloProfilePreferences.setAuthCode("");*/
        Timber.d(" Authen Zalo Error message %s error %s", message, errorCode);
        if (mView != null) { // chua destroy view
            if (!NetworkHelper.isNetworkAvailable(applicationContext)) {
                showErrorView(applicationContext.getString(R.string.exception_no_connection_try_again));
                zpAnalytics.trackEvent(ZPEvents.LOGINFAILED_NONETWORK);
            } else if (errorCode == -1111) {
                Timber.d("onAuthError User click backpress");
            } else {
                if (TextUtils.isEmpty(message)) {
                    message = applicationContext.getString(R.string.exception_login_zalo_error);
                }
                showErrorView(message);
                zpAnalytics.trackEvent(ZPEvents.LOGINFAILED_USERDENIED);
            }
            hideLoadingView();
        }
    }

    @Override
    public void onGetOAuthComplete(long zaloId, String authCode, String channel) {
        Timber.d("OAuthComplete uid: %s authCode: %s", zaloId, authCode);

        userConfig.saveUserInfo(zaloId, "", "", 0, 0);
        if (mView != null) {
            this.getZaloProfileInfo();
            this.loginPayment(zaloId, authCode);
        }

        zpAnalytics.trackEvent(ZPEvents.LOGINSUCCESS_ZALO);
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


    private void loginPayment(long zuid, String zalooauthcode) {
        showLoadingView();
        subscriptionLogin = passportRepository.login(zuid, zalooauthcode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LoginPaymentSubscriber());
    }

    private void showErrorView(String message) {
        mView.showError(message);
    }

    private void gotoHomeScreen() {
        mView.gotoMainActivity();
    }

    private void gotoUpdateProfileLevel2() {
        mView.gotoUpdateProfileLevel2();
    }

    private final void onLoginSuccess(User user) {
        Timber.d("session %s uid %s need_invitation %s", user.accesstoken, user.uid, user.need_invitation);
        // Khởi tạo user component

        if (user.need_invitation == 1) {
            zpAnalytics.trackEvent(ZPEvents.NEEDINVITATIONCODE);
            zpAnalytics.trackEvent(ZPEvents.INVITATIONFROMLOGIN);
        } else {
            AndroidApplication.instance().createUserComponent(user);
            this.gotoHomeScreen();
            zpAnalytics.trackEvent(ZPEvents.APPLAUNCHHOMEFROMLOGIN);
        }
    }

    private final void onLoginError(Throwable e) {
        if (e instanceof InvitationCodeException) {
            mView.gotoInvitationCode();
        } else {

            Timber.w(e, "exception  ");
            hideLoadingView();
            String message = ErrorMessageFactory.create(applicationContext, e);
            showErrorView(message);
            zpAnalytics.trackEvent(ZPEvents.LOGINFAILED_API_ERROR);
        }

    }

    private final class LoginPaymentSubscriber extends DefaultSubscriber<User> {
        public LoginPaymentSubscriber() {
        }

        @Override
        public void onNext(User user) {
            Timber.d("login success " + user);
            // TODO: Use your own attributes to track content views in your app
            Answers.getInstance().logLogin(new LoginEvent().putSuccess(true));

            LoginPresenter.this.onLoginSuccess(user);
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                // simply ignore the error
                // because it is handled from event subscribers

                if (e instanceof ServerMaintainException) {
                    LoginPresenter.this.hideLoadingView();
                }

                return;
            }

            LoginPresenter.this.onLoginError(e);
        }
    }

}

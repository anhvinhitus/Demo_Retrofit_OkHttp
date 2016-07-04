package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;
import android.content.Intent;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;
import com.zing.zalo.zalosdk.oauth.LoginVia;
import com.zing.zalo.zalosdk.oauth.ZaloOpenAPICallback;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import org.json.JSONObject;

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
    }

    @Override
    public void destroyView() {
        hideLoadingView();
        this.mView = null;
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
        Timber.d("Destroy presenter");
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
        showLoadingView();
        ZaloSDK.Instance.authenticate(activity, LoginVia.APP_OR_WEB, new LoginListener(this));
    }

    @Override
    public void onAuthenError(int errorCode, String message) {

     /*   zaloProfilePreferences.setUserId(0);
        zaloProfilePreferences.setAuthCode("");*/
        Timber.d(" Authen Zalo Error message %s error %s", message, errorCode);
        if (mView != null) { // chua destroy view
            if (mView.getContext() != null && !NetworkHelper.isNetworkAvailable(mView.getContext())) {
                showErrorView(mView.getContext().getString(R.string.exception_no_connection_try_again));
                zpAnalytics.trackEvent(ZPEvents.LOGINFAILED_NONETWORK);
            } else {
                showErrorView(message);
                zpAnalytics.trackEvent(ZPEvents.LOGINFAILED_USERDENIED);
            }
            hideLoadingView();
        }
    }

    @Override
    public void onGetOAuthComplete(long uId, String authCode, String channel) {

     /*       zaloProfilePreferences.setUserId(uId);
        zaloProfilePreferences.setAuthCode(authCode);
*/
        userConfig.saveUserInfo(uId, "", "", 0, 0);

        Timber.d("OAuthComplete uid: %s authCode: %s", uId, authCode);
        this.getZaloProfileInfo();
        this.loginPayment(uId, authCode);
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

    private void getZaloProfileInfo() {
        ZaloSDK.Instance.getProfile(applicationContext, new ZaloOpenAPICallback() {
            @Override
            public void onResult(JSONObject profile) {
                try {
                    userConfig.saveZaloUserInfo(profile);
                } catch (Exception ex) {
                    Timber.w(ex, " Exception :");
                }
            }
        });
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
        hideLoadingView();
        String message = ErrorMessageFactory.create(applicationContext, e);
        showErrorView(message);
        zpAnalytics.trackEvent(ZPEvents.LOGINFAILED_API_ERROR);
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
                return;
            }

            Timber.w(e, "onError " + e);
            LoginPresenter.this.onLoginError(e);
        }
    }


}

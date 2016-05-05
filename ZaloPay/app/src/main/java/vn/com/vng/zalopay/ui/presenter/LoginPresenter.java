package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;
import android.content.Intent;

import com.zing.zalo.zalosdk.oauth.LoginVia;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.account.network.listener.LoginListener;
import vn.com.vng.zalopay.account.utils.ZaloProfilePreferences;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.internal.di.modules.user.UserModule;
import vn.com.vng.zalopay.ui.view.ILoginView;

/**
 * Created by AnhHieu on 3/26/16.
 */

@Singleton
public final class LoginPresenter extends BaseAppPresenter implements Presenter<ILoginView>, LoginListener.ILoginZaloListener {

    private ILoginView mView;

    private ZaloProfilePreferences zaloProfilePreferences;

    private Subscription subscriptionLogin;

    @Inject
    public LoginPresenter(ZaloProfilePreferences zaloProfilePreferences) {
        this.zaloProfilePreferences = zaloProfilePreferences;
    }

    @Override
    public void setView(ILoginView view) {
        this.mView = view;
    }

    @Override
    public void destroyView() {
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
    }

    private void unsubscribe() {
        unsubscribeIfNotNull(subscriptionLogin);
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        try {
            ZaloSDK.Instance.onActivityResult(activity, requestCode, resultCode, data);
        } catch (Exception ex) {
            Timber.e(ex, " message " + ex.getMessage());
        }
    }

    public void loginZalo(Activity activity) {
        showLoadingView();
        ZaloSDK.Instance.authenticate(activity, LoginVia.APP_OR_WEB, new LoginListener(this));
    }

    @Override
    public void onAuthenError(int errorCode, String message) {
        Timber.tag("LoginPresenter").d("onAuthenError................message %s error %s", message, errorCode);
        zaloProfilePreferences.setUserId(0);
        zaloProfilePreferences.setAuthCode("");
        showErrorView(message);
        hideLoadingView();
    }

    @Override
    public void onGetOAuthComplete(long uId, String authCode, String channel) {
        Timber.tag("LoginPresenter").d("onGetOAuthComplete................authCode:" + authCode);
        zaloProfilePreferences.setUserId(uId);
        zaloProfilePreferences.setAuthCode(authCode);

        //Fixme :  dang test
        HashMap map = AndroidApplication.instance().getAppComponent().paramsRequestProvider().getParamsZalo();
        map.put("appid", String.valueOf(1));
        map.put("userid", String.valueOf(uId));
        map.put("zalooauthcode", authCode);

        this.loginPayment(uId, authCode);

    }


    private void showLoadingView() {
        mView.showLoading();
    }

    private void hideLoadingView() {
        mView.hideLoading();
    }


    private void loginPayment(long zuid, String zalooauthcode) {
        subscriptionLogin = passportRepository.login(zuid, zalooauthcode)
                .subscribe(new LoginPaymentSubscriber());
    }

    private void showErrorView(String message) {
        mView.showError(message);
    }

    private void gotoHomeScreen() {
        mView.gotoMainActivity();
    }

    private final void onLoginSuccess(User user) {
        Timber.d("session " + user.accesstoken);
        // Khởi tạo user component
        AndroidApplication.instance()
                .getAppComponent().plus(new UserModule(user));

        this.hideLoadingView();
        this.gotoHomeScreen();
    }


    private final void onLoginError(Throwable e) {
        hideLoadingView();
        String message = ErrorMessageFactory.create(mView.getContext(), e);
        showErrorView(message);
    }


    private final class LoginPaymentSubscriber extends DefaultSubscriber<User> {
        public LoginPaymentSubscriber() {
        }

        @Override
        public void onNext(User user) {
            Timber.d("login success " + user);
            LoginPresenter.this.onLoginSuccess(user);
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Timber.e(e, "onError " + e);
            LoginPresenter.this.onLoginError(e);
        }
    }


}
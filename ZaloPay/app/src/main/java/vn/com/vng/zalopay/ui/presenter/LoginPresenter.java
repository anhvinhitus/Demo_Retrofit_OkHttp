package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;
import android.content.Intent;

import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;
import vn.com.vng.vmpay.account.network.listener.LoginListener;
import vn.com.vng.vmpay.account.utils.ZaloProfilePreferences;
import vn.com.vng.zalopay.ui.view.ILoginView;

/**
 * Created by AnhHieu on 3/26/16.
 */

@Singleton
public final class LoginPresenter extends BaseAppPresenter implements Presenter<ILoginView>, LoginListener.ILoginZaloListener {

    private ILoginView mView;

//    private UseCase loginUseCase;
    private ZaloProfilePreferences zaloProfilePreferences;

    @Inject
    public LoginPresenter(/*@Named("Login") UseCase login,*/ ZaloProfilePreferences zaloProfilePreferences) {
//        this.loginUseCase = login;
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
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        try {
            ZaloSDK.Instance.onActivityResult(activity, requestCode, resultCode, data);
        } catch (Exception ex) {
            Timber.e(ex, " message " + ex.getMessage());
        }
    }

    public void loginZalo(Activity activity) {
        ZaloSDK.Instance.authenticate(activity, new LoginListener(this));
    }

    @Override
    public void onAuthenError(int errorCode, String message) {
        Timber.tag("LoginPresenter").d("onAuthenError................errorCode:" + errorCode);
        Timber.tag("LoginPresenter").d("onAuthenError................message:" + message);
        zaloProfilePreferences.setUserId(0);
        zaloProfilePreferences.setAuthCode("");
        mView.hideLoading();
        mView.showError(message);
    }

    @Override
    public void onGetOAuthComplete(long uId, String authCode, String channel) {
        Timber.tag("LoginPresenter").d("onGetOAuthComplete................authCode:" + authCode);
        zaloProfilePreferences.setUserId(uId);
        zaloProfilePreferences.setAuthCode(authCode);
        mView.hideLoading();
        mView.gotoMainActivity();
    }
}
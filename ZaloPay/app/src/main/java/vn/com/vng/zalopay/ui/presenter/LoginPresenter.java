package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

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
import vn.com.vng.zalopay.account.network.listener.LoginListener;
import vn.com.vng.zalopay.account.utils.ZaloProfilePreferences;
import vn.com.vng.zalopay.data.cache.UserConfig;
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

    private ZaloProfilePreferences zaloProfilePreferences;

    private Subscription subscriptionLogin;

    private Context context;

    private UserConfig userConfig;

    @Inject
    public LoginPresenter(Context context, ZaloProfilePreferences zaloProfilePreferences, UserConfig userConfig) {
        this.zaloProfilePreferences = zaloProfilePreferences;
        this.context = context;
        this.userConfig = userConfig;
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

     /*   zaloProfilePreferences.setUserId(0);
        zaloProfilePreferences.setAuthCode("");*/
        Timber.tag(TAG).d(" Authen Zalo Error message %s error %s", message, errorCode);
        if (mView != null) { // chua destroy view
            showErrorView(message);
            hideLoadingView();
        }
    }

    @Override
    public void onGetOAuthComplete(long uId, String authCode, String channel) {

     /*       zaloProfilePreferences.setUserId(uId);
        zaloProfilePreferences.setAuthCode(authCode);
*/
        userConfig.saveUserInfo(uId, "", "", 0, 0);

        Timber.tag(TAG).d("OAuthComplete uid %s authCode %s", uId, authCode);
        if (mView != null) {
            this.getZaloProfileInfo();
            this.loginPayment(uId, authCode);
        }
    }


    private void showLoadingView() {
        mView.showLoading();
    }

    private void hideLoadingView() {
        mView.hideLoading();
    }


    private void loginPayment(long zuid, String zalooauthcode) {
        subscriptionLogin = passportRepository.login(zuid, zalooauthcode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LoginPaymentSubscriber());
    }

    private void getZaloProfileInfo() {
        ZaloSDK.Instance.getProfile(context, new ZaloOpenAPICallback() {
            @Override
            public void onResult(JSONObject profile) {
                try {
                    JSONObject data = profile.getJSONObject("result");

                    Timber.tag(TAG).d("zalo profile %s", data.toString());

                    long userId = data.getLong("userId");
                    String displayName = data.getString("displayName");
                    String avatar = data.getString("largeAvatar");
                    long birthday = data.getLong("birthDate");
                    int userGender = data.getInt("userGender");

                    userConfig.saveUserInfo(userId, avatar, displayName, birthday, userGender);

                } catch (Exception ex) {
                    Timber.tag(TAG).e(ex, " Exception :");
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

    private final void onLoginSuccess(User user) {
        Timber.d("session " + user.accesstoken);
        Timber.d("uid " + user.uid);
        // Khởi tạo user component
        AndroidApplication.instance().createUserComponent(user);
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
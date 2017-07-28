package vn.com.vng.zalopay.passport;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;
import com.zalopay.ui.widget.dialog.SweetAlertDialog;
import com.zing.zalo.zalosdk.oauth.LoginVia;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.network.listener.LoginListener;
import vn.com.vng.zalopay.account.network.listener.ZaloErrorCode;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.exception.AccountSuspendedException;
import vn.com.vng.zalopay.data.exception.InvitationCodeException;
import vn.com.vng.zalopay.data.exception.RequirePhoneException;
import vn.com.vng.zalopay.data.exception.ServerMaintainException;
import vn.com.vng.zalopay.data.exception.TokenException;
import vn.com.vng.zalopay.data.zalosdk.IProfileCallback;
import vn.com.vng.zalopay.data.zalosdk.ZaloSdkApi;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.zalosdk.ZaloProfile;
import vn.com.vng.zalopay.domain.repository.ApplicationSession;
import vn.com.vng.zalopay.domain.repository.PassportRepository;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.network.NetworkHelper;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.utils.AppVersionUtils;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;


/**
 * Created by AnhHieu on 3/26/16.
 * *
 */

public class LoginPresenter extends AbstractLoginPresenter<ILoginView> implements LoginListener.ILoginZaloListener {

    private final UserConfig mUserConfig;
    private final PassportRepository mPassportRepository;
    private final ApplicationSession mApplicationSession;
    private final AppResourceStore.Repository mAppResourceRepository;
    private final ZaloSdkApi mZaloSdkApi;
    private final Context mApplicationContext;

    @Inject
    LoginPresenter(Context applicationContext, UserConfig userConfig, PassportRepository passportRepository, ApplicationSession applicationSession, AppResourceStore.Repository appResourceRepository, ZaloSdkApi zaloSdkApi) {
        mUserConfig = userConfig;
        mPassportRepository = passportRepository;
        mApplicationSession = applicationSession;
        mAppResourceRepository = appResourceRepository;
        mZaloSdkApi = zaloSdkApi;
        mApplicationContext = applicationContext;
    }

    private boolean mIsCallingExternal = false;
    private LoginListener mLoginListener = new LoginListener(this);

    public void detachView() {
        hideLoadingView();
        super.detachView();
    }

    public void resume() {

        Timber.d("resume has current user [%s]", mUserConfig.hasCurrentUser());
        UserComponent userComponent = ((AndroidApplication) mApplicationContext).getUserComponent();
        if (userComponent == null) {
            return;
        }

        Timber.d("go to home screen ignore login screen");
        mView.gotoHomePage();
        mView.finish();
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        try {
            ZaloSDK.Instance.onActivityResult(activity, requestCode, resultCode, data);
        } catch (Exception ex) {
            Timber.w(ex, "message %s", ex.getMessage());
        }

    }

    void loginZalo(Activity activity) {
        ZPAnalytics.trackEvent(ZPEvents.TAP_LOGIN);

        if (AppVersionUtils.showDialogForceUpgradeApp((Activity) mView.getContext())) {
            return;
        }

        if (!NetworkHelper.isNetworkAvailable(mApplicationContext)) {
            showNetworkError();
            return;
        }

        if (mIsCallingExternal && loginCallingExternal(activity)) {
            return;
        }

        try {
            ZaloSDK.Instance.authenticate(activity, LoginVia.APP_OR_WEB, mLoginListener);
        } catch (Exception e) {
            Timber.w(e, "Authenticate to login zalo throw exception.");
        }

    }

    private boolean loginCallingExternal(Activity activity) {
        Intent intent = activity.getIntent();

        long zaloId = intent.getLongExtra("zaloid", 0);
        String zaloOAuthCode = intent.getStringExtra("zauthcode");

        if (zaloId > 0 && !TextUtils.isEmpty(zaloOAuthCode)) {
            ZaloProfile profile = new ZaloProfile(zaloId, "", "", 0, 0, "");
            mUserConfig.saveUserInfo(profile.userId, profile.avatar, profile.displayName, profile.birthDate, profile.userGender, profile.userName);
            loginPayment(profile, zaloOAuthCode);
            return true;
        }

        return false;
    }

    public void onAuthError(int errorCode, String message) {
        Timber.d(" Authen Zalo Error message %s error %s", message, errorCode);
        if (mView == null) { // View đã bị destroy
            return;
        }

        hideLoadingView();

        if (!NetworkHelper.isNetworkAvailable(mApplicationContext) || errorCode == ZaloErrorCode.RESULTCODE_NETWORK_ERROR) {
            showNetworkError();
            ZPAnalytics.trackEvent(ZPEvents.LOGINFAILED_NONETWORK);
        } else if (errorCode == ZaloErrorCode.RESULTCODE_USER_CANCEL ||
                errorCode == ZaloErrorCode.RESULTCODE_USER_BACK ||
                errorCode == ZaloErrorCode.RESULTCODE_USER_REJECT ||
                errorCode == ZaloErrorCode.RESULTCODE_USER_BACK_BUTTON) {
            Timber.d("onAuthError User click backpress");
        } else {
            String msg = mApplicationContext.getString(R.string.exception_login_zalo_error);
            showErrorView(msg);
            ZPAnalytics.trackEvent(ZPEvents.LOGINFAILED_USERDENIED);
        }
    }

    public void onGetOAuthComplete(long zaloId, String authCode, String channel) {
        Timber.d("Authentication zalo success [uid: %s authCode: %s]", zaloId, authCode);

        ZPAnalytics.trackEvent(ZPEvents.LOGIN_RESULT);
        mUserConfig.saveUserInfo(zaloId, "", "", 0, 0, "");

        showLoadingView();
        mZaloSdkApi.getProfile(new GetProfileZaloCallBack(authCode));
    }

    private class GetProfileZaloCallBack implements IProfileCallback {

        private final String authCode;

        GetProfileZaloCallBack(String authCode) {
            this.authCode = authCode;
        }

        public void onGetProfile(ZaloProfile profile) {
            loginPayment(profile, authCode);
        }

        public void onGetProfileFailure() {
            hideLoadingView();
            showErrorView(mApplicationContext.getString(R.string.get_zalo_profile_failure));
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

    private void showErrorView(String message) {
        if (mView != null) {
            mView.showError(message);
        }
    }

    private void showNetworkError() {
        if (mView != null) {
            mView.showNetworkErrorDialog();
        }
    }

    private void showMessageDialog(String message) {
        if (mView != null) {
            mView.showCustomDialog(message,
                    mApplicationContext.getString(R.string.txt_close),
                    SweetAlertDialog.NORMAL_TYPE, null);
        }
    }

    private void loginPayment(ZaloProfile profile, String oauthcode) {
        Timber.d("login payment system");
        Subscription subscriptionLogin = mPassportRepository.login(profile.userId, oauthcode)
                .doOnNext(user -> mApplicationSession.clearMerchantSession())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LoginPaymentSubscriber(profile, oauthcode));
        mSubscription.add(subscriptionLogin);
    }

    private class LoginPaymentSubscriber extends DefaultSubscriber<User> {

        private final ZaloProfile profile;
        private final String oauthcode;

        private LoginPaymentSubscriber(ZaloProfile profile, String oauthcode) {
            this.profile = profile;
            this.oauthcode = oauthcode;
        }

        public void onStart() {
            showLoadingView();
        }

        public void onNext(User user) {
            onAuthenticated(user);
            ZPAnalytics.trackEvent(ZPEvents.LOGIN_RESULT);
        }

        public void onError(Throwable e) {
            onAuthenticationError(e, profile, oauthcode);
        }
    }

    private void onAuthenticationError(Throwable e, ZaloProfile profile, String oauthcode) {
        if (mView == null) {
            Timber.w("View login screen is NULL");
            return;
        }

        hideLoadingView();

        if (e instanceof TokenException
                || e instanceof AccountSuspendedException
                || e instanceof ServerMaintainException) {
            showMessageDialog(ErrorMessageFactory.create(mApplicationContext, e));
        } else if (e instanceof InvitationCodeException) {
            mView.gotoInvitationCode();
            ZPAnalytics.trackEvent(ZPEvents.NEEDINVITATIONCODE);
            ZPAnalytics.trackEvent(ZPEvents.INVITATIONFROMLOGIN);
        } else if (e instanceof RequirePhoneException) {
            mView.gotoOnboarding(profile, oauthcode);
        } else {
            Timber.d("Login error [message %s]", e.getMessage());
            String message = ErrorMessageFactory.create(mApplicationContext, e);
            showErrorView(message);
            ZPAnalytics.trackEvent(ZPEvents.LOGINFAILED_API_ERROR);
        }
    }

    void fetchAppResource() {
        Subscription fetchSubscription = mAppResourceRepository.fetchAppResource()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mSubscription.add(fetchSubscription);
    }

}

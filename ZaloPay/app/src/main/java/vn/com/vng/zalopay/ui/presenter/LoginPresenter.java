package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;
import com.zing.zalo.zalosdk.oauth.LoginVia;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.network.listener.LoginListener;
import vn.com.vng.zalopay.account.network.listener.ZaloErrorCode;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.exception.InvitationCodeException;
import vn.com.vng.zalopay.data.exception.ServerMaintainException;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.data.zalosdk.ZaloSdkApi;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ApplicationSession;
import vn.com.vng.zalopay.domain.repository.PassportRepository;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.service.GlobalEventHandlingService;
import vn.com.vng.zalopay.ui.view.ILoginView;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by AnhHieu on 3/26/16.
 * *
 */

public final class LoginPresenter extends AbstractPresenter<ILoginView> implements LoginListener.ILoginZaloListener {
    private LoginListener mLoginListener = new LoginListener(this);
    private Context mApplicationContext;
    private UserConfig mUserConfig;
    private PassportRepository mPassportRepository;
    private ApplicationSession mApplicationSession;
    private GlobalEventHandlingService mGlobalEventService;

    private boolean mIsCallingExternal;

    private final AppResourceStore.Repository mAppResourceRepository;

    @Inject
    ZaloSdkApi mZaloSdkApi;

    @Inject
    LoginPresenter(Context applicationContext,
                   UserConfig userConfig,
                   PassportRepository passportRepository,
                   ApplicationSession applicationSession,
                   GlobalEventHandlingService globalEventHandlingService,
                   AppResourceStore.Repository appResourceRepository) {

        this.mApplicationContext = applicationContext;
        this.mUserConfig = userConfig;
        this.mPassportRepository = passportRepository;
        this.mApplicationSession = applicationSession;
        this.mGlobalEventService = globalEventHandlingService;
        this.mAppResourceRepository = appResourceRepository;

    }

    @Override
    public void detachView() {
        hideLoadingView();
        super.detachView();
    }

    @Override
    public void resume() {

        Timber.d("resume has current user [%s]", mUserConfig.hasCurrentUser());
        UserComponent userComponent = ((AndroidApplication) mApplicationContext).getUserComponent();

        if (mUserConfig.hasCurrentUser() && userComponent != null) {
            Timber.d("go to home screen ignore login screen");
            gotoHomeScreen();
            return;
        }

        GlobalEventHandlingService.Message message = mGlobalEventService.popMessageAtLogin();
        if (message == null) {
            return;
        }

        if (mView != null) {
            mView.showCustomDialog(message.content,
                    mApplicationContext.getString(R.string.txt_close),
                    message.messageType,
                    null);
        }
    }

    public void setCallingExternal(boolean isCallingExternal) {
        mIsCallingExternal = isCallingExternal;
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        try {
            ZaloSDK.Instance.onActivityResult(activity, requestCode, resultCode, data);
        } catch (Exception ex) {
            Timber.w(ex, "message %s", ex.getMessage());
        }
    }

    public void loginZalo(Activity activity) {

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
        if (intent == null) {
            return false;
        }

        long zaloId = intent.getLongExtra("zaloid", 0);
        String zaloOAuthCode = intent.getStringExtra("zauthcode");

        if (zaloId > 0 && !TextUtils.isEmpty(zaloOAuthCode)) {
            mUserConfig.saveUserInfo(zaloId, "", "", 0, 0);
            loginPayment(zaloId, zaloOAuthCode);
            return true;
        }

        return false;
    }

    @Override
    public void onAuthError(int errorCode, String message) {
        Timber.d(" Authen Zalo Error message %s error %s", message, errorCode);
        if (mView != null) { // chua destroy view
            if (!NetworkHelper.isNetworkAvailable(mApplicationContext)
                    || errorCode == ZaloErrorCode.RESULTCODE_NETWORK_ERROR) {
                showNetworkError();
                ZPAnalytics.trackEvent(ZPEvents.LOGINFAILED_NONETWORK);
            } else if (errorCode == ZaloErrorCode.RESULTCODE_USER_CANCEL ||
                    errorCode == ZaloErrorCode.RESULTCODE_USER_BACK ||
                    errorCode == ZaloErrorCode.RESULTCODE_USER_REJECT ||
                    errorCode == ZaloErrorCode.RESULTCODE_USER_BACK_BUTTON) {
                Timber.d("onAuthError User click backpress");
            } else {
                Timber.w("Authen Zalo error, code: %s message: %s", errorCode, message);
                message = mApplicationContext.getString(R.string.exception_login_zalo_error);
                showErrorView(message);
                ZPAnalytics.trackEvent(ZPEvents.LOGINFAILED_USERDENIED);
            }
            hideLoadingView();
        }
    }

    @Override
    public void onGetOAuthComplete(long zaloId, String authCode, String channel) {
        Timber.d("OAuthComplete uid: %s authCode: %s", zaloId, authCode);

        mUserConfig.saveUserInfo(zaloId, "", "", 0, 0);
        if (mView != null) {
            mZaloSdkApi.getProfile();
            this.loginPayment(zaloId, authCode);
        }

        ZPAnalytics.trackEvent(ZPEvents.LOGINSUCCESS_ZALO);
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
        Subscription subscriptionLogin = mPassportRepository.login(zuid, zalooauthcode)
                .doOnNext(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        mApplicationSession.clearMerchantSession();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LoginPaymentSubscriber());
        mSubscription.add(subscriptionLogin);
    }

    private void showErrorView(String message) {
        if (mView != null) {
            mView.showError(message);
        }
    }

    private void showNetworkError() {
        if (mView != null) {
            mView.showNetworkError();
        }
    }

    private void gotoHomeScreen() {
        if (mView != null) {
            mView.gotoMainActivity();
        }
    }

    private void onLoginSuccess(User user) {
        Timber.d("onLoginSuccess: %s", mView);
        if (mView == null) {
            return;
        }

        Timber.d("session %s zaloPayId %s", user.accesstoken, user.zaloPayId);
        AndroidApplication.instance().createUserComponent(user);

        if (mIsCallingExternal) {
            sendResultSuccess(mView.getActivity());
        } else {
            gotoHomeScreen();
            ZPAnalytics.trackEvent(ZPEvents.APPLAUNCHHOMEFROMLOGIN);
        }

    }

    private void sendResultSuccess(Activity activity) {
        Intent oldIntent = activity.getIntent();
        if (oldIntent == null) {
            activity.finish();
            return;
        }

        Intent intent = new Intent();
        if (oldIntent.getData() != null) {
            intent.setData(oldIntent.getData());
        }

        try {
            PendingIntent pi = oldIntent.getParcelableExtra("pendingResult");
            if (pi != null) {
                pi.send(activity, Activity.RESULT_OK, intent);
            } else {
                activity.setResult(Activity.RESULT_OK, intent);
            }
        } catch (Exception e) {
            Timber.d(e, "sendResultSuccess: ");
        } finally {
            activity.finish();
        }

    }

    private void onLoginError(Throwable e) {
        hideLoadingView();
        if (e instanceof InvitationCodeException) {
            mView.gotoInvitationCode();
            ZPAnalytics.trackEvent(ZPEvents.NEEDINVITATIONCODE);
            ZPAnalytics.trackEvent(ZPEvents.INVITATIONFROMLOGIN);
        } else {
            Timber.w(e, "exception");
            String message = ErrorMessageFactory.create(mApplicationContext, e);
            showErrorView(message);
            ZPAnalytics.trackEvent(ZPEvents.LOGINFAILED_API_ERROR);
        }
    }

    private final class LoginPaymentSubscriber extends DefaultSubscriber<User> {

        @Override
        public void onStart() {
            showLoadingView();
        }

        @Override
        public void onNext(User user) {
            Timber.d("login success %s", user);
            // TODO: Use your own attributes to track content views in your app
            Answers.getInstance().logLogin(new LoginEvent().putSuccess(true));

            LoginPresenter.this.onLoginSuccess(user);
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

    public void fetchAppResource() {
        Subscription subscription = mAppResourceRepository.ensureAppResourceAvailable()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mSubscription.add(subscription);

        Subscription fetchSubscription = mAppResourceRepository.fetchAppResource()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mSubscription.add(fetchSubscription);
    }

}

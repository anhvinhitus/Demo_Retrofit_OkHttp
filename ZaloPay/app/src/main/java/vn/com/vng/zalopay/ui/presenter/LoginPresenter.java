package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;
import com.zing.zalo.zalosdk.oauth.LoginVia;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.network.listener.LoginListener;
import vn.com.vng.zalopay.account.network.listener.ZaloErrorCode;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.exception.InvitationCodeException;
import vn.com.vng.zalopay.data.exception.ServerMaintainException;
import vn.com.vng.zalopay.data.util.NetworkHelper;
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

public final class LoginPresenter extends BaseAppPresenter implements IPresenter<ILoginView>, LoginListener.ILoginZaloListener {

    private ILoginView mView;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    private LoginListener mLoginListener = new LoginListener(this);
    private Context mApplicationContext;
    private UserConfig mUserConfig;
    private PassportRepository mPassportRepository;
    private ApplicationSession mApplicationSession;
    private GlobalEventHandlingService mGlobalEventService;
    private Uri mData;

    @Inject
    LoginPresenter(Context applicationContext,
                   UserConfig userConfig,
                   PassportRepository passportRepository,
                   ApplicationSession applicationSession,
                   GlobalEventHandlingService globalEventHandlingService) {

        this.mApplicationContext = applicationContext;
        this.mUserConfig = userConfig;
        this.mPassportRepository = passportRepository;
        this.mApplicationSession = applicationSession;
        this.mGlobalEventService = globalEventHandlingService;
    }

    @Override
    public void attachView(ILoginView view) {
        this.mView = view;
        Timber.d("set login view");
    }

    @Override
    public void detachView() {
        hideLoadingView();
        this.mView = null;
        Timber.d("detachView");
    }

    @Override
    public void resume() {

        Timber.d("resume has current user [%s]", mUserConfig.hasCurrentUser());
        UserComponent userComponent = ((AndroidApplication) mApplicationContext).getUserComponent();
        // Trường hợp user login ở merchant app sau đó quay lại zalopay
        // TODO: 12/10/16 kiểm tra lại trường hợp user đang login (chưa kết thúc quá trình đã qua zalopay)
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

    @Override
    public void pause() {
    }

    @Override
    public void destroy() {
        this.detachView();
        this.unsubscribe();
    }

    public void setData(Uri data) {
        this.mData = data;
    }

    private void unsubscribe() {
        unsubscribeIfNotNull(compositeSubscription);
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        try {
            ZaloSDK.Instance.onActivityResult(activity, requestCode, resultCode, data);
        } catch (Exception ex) {
            Timber.w(ex, " message " + ex.getMessage());
        }
    }

    public void loginZalo(Activity activity) {
        if (NetworkHelper.isNetworkAvailable(mApplicationContext)) {
            ZaloSDK.Instance.authenticate(activity, LoginVia.APP_OR_WEB, mLoginListener);
        } else {
            showNetworkError();
        }
    }

    @Override
    public void onAuthError(int errorCode, String message) {
        Timber.d(" Authen Zalo Error message %s error %s", message, errorCode);
        if (mView != null) { // chua destroy view
            if (!NetworkHelper.isNetworkAvailable(mApplicationContext)) {
                showErrorView(mApplicationContext.getString(R.string.exception_no_connection_try_again));
                ZPAnalytics.trackEvent(ZPEvents.LOGINFAILED_NONETWORK);
            } else if (errorCode == ZaloErrorCode.RESULTCODE_USER_CANCEL ||
                    errorCode == ZaloErrorCode.RESULTCODE_USER_BACK ||
                    errorCode == ZaloErrorCode.RESULTCODE_USER_REJECT ||
                    errorCode == ZaloErrorCode.RESULTCODE_USER_BACK_BUTTON) {
                Timber.d("onAuthError User click backpress");
            } else {
//                if (TextUtils.isEmpty(message)) {
//                    message = mApplicationContext.getString(R.string.exception_login_zalo_error);
//                }
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
            this.getZaloProfileInfo(mApplicationContext, mUserConfig);
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
        showLoadingView();
        Subscription subscriptionLogin = mPassportRepository.login(zuid, zalooauthcode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LoginPaymentSubscriber());
        compositeSubscription.add(subscriptionLogin);
    }

    private void showErrorView(String message) {
        mView.showError(message);
    }

    private void showNetworkError() {
        if (mView == null) {
            return;
        }
        mView.showNetworkError();
    }

    private void gotoHomeScreen() {
        if (mView != null) {
            mView.gotoMainActivity();
        }
    }

    private void onLoginSuccess(User user) {
        Timber.d("session %s zaloPayId %s", user.accesstoken, user.zaloPayId);
        AndroidApplication.instance().createUserComponent(user);
        if (mData != null) {
            Activity act = mView.getActivity();
            Intent intent = new Intent();
            intent.setData(mData);
            act.setResult(Activity.RESULT_OK, intent);
            mView.getActivity().finish();
        } else {
            this.gotoHomeScreen();
        }
        clearMerchant();
        ZPAnalytics.trackEvent(ZPEvents.APPLAUNCHHOMEFROMLOGIN);
    }

    private void onLoginError(Throwable e) {
        hideLoadingView();
        if (e instanceof InvitationCodeException) {
            clearMerchant();
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
        public void onNext(User user) {
            Timber.d("login success %s", user);
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

    private void clearMerchant() {
        Subscription subscription = mApplicationSession.clearMerchant()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>());
        compositeSubscription.add(subscription);
    }
}

package vn.com.vng.zalopay.react;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.zalopay.apploader.ReactBasedActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.app.AppLifeCycle;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.eventbus.ThrowToLoginScreenEvent;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.event.ForceUpdateAppEvent;
import vn.com.vng.zalopay.event.TokenPaymentExpiredEvent;
import vn.com.vng.zalopay.event.UncaughtRuntimeExceptionEvent;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.passport.LoginZaloActivity;

/**
 * Created by hieuvm on 6/28/17.
 * *
 */

public abstract class UserReactBasedActivity extends ReactBasedActivity {

    protected final String TAG = getClass().getSimpleName();

    protected void onUserComponentSetup(@NonNull UserComponent userComponent) {

    }

    protected void setupActivityComponent(ApplicationComponent applicationComponent) {
        setupUserComponent(applicationComponent);
    }

    private boolean isUserSessionStarted;
    private boolean restarted;
    protected final EventBus eventBus = getAppComponent().eventBus();
    protected final Navigator navigator = getAppComponent().navigator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restarted = savedInstanceState != null;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (eventBus.isRegistered(this)) {
            eventBus.unregister(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!eventBus.isRegistered(this)) {
            eventBus.register(this);
        }
    }

    @Override
    protected void doInjection() {
        setupActivityComponent(getAppComponent());
    }

    private void setupUserComponent(ApplicationComponent applicationComponent) {
        isUserSessionStarted = createUserComponent(applicationComponent);

        if (isUserSessionStarted) {
            onUserComponentSetup(getUserComponent());
            return;
        }

        if (!AppLifeCycle.isLastActivity(LoginZaloActivity.class.getSimpleName())) {
            navigator.startLoginActivity(this, true);
        }

        finish();

    }

    private boolean createUserComponent(ApplicationComponent applicationComponent) {

        if (getUserComponent() != null) {
            return true;
        }

        UserConfig userConfig = applicationComponent.userConfig();
        if (userConfig.isSignIn()) {
            userConfig.loadConfig();
            AndroidApplication.instance().createUserComponent(userConfig.getCurrentUser());
        }

        Timber.e("Create UserComponent is NPE in %s. @Injected dependencies can be null - Application restarted [%s] - activities size [%s]", TAG, restarted, AppLifeCycle.activities.size());
        return getUserComponent() != null;
    }

    protected UserComponent getUserComponent() {
        return AndroidApplication.instance().getUserComponent();
    }

    protected ApplicationComponent getAppComponent() {
        return AndroidApplication.instance().getAppComponent();
    }

    protected boolean isUserSessionStarted() {
        return isUserSessionStarted;
    }

    @Subscribe
    public void onUncaughtRuntimeException(UncaughtRuntimeExceptionEvent event) {
        reactInstanceCaughtError();
        handleException(event.getInnerException());
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onThrowToLoginScreen(ThrowToLoginScreenEvent event) {
        Timber.d("onThrowToLoginScreen: in Screen %s ", TAG);
        User user = getAppComponent().userConfig().getCurrentUser();
        clearUserSession(ErrorMessageFactory.create(this, event.getThrowable(), user));
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onTokenPaymentExpired(TokenPaymentExpiredEvent event) {
        Timber.i("SESSION EXPIRED in Screen %s", TAG);
        clearUserSession(getString(R.string.exception_token_expired_message));
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onForceUpdateApp(ForceUpdateAppEvent event) {
        Timber.i("Force update app in Screen %s", TAG);
        clearUserSession(null);
    }

    protected boolean clearUserSession(String message) {
        getAppComponent().applicationSession().setMessageAtLogin(message);
        getAppComponent().applicationSession().clearUserSession();
        return true;
    }
}

package vn.com.vng.zalopay.user;

import android.os.Bundle;
import android.support.annotation.NonNull;

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
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.passport.LoginZaloActivity;
import vn.com.vng.zalopay.ui.activity.BaseActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by hieuvm on 4/17/17.
 * UserBaseActivity
 */

public abstract class UserBaseActivity extends BaseActivity {

    protected void onUserComponentSetup(@NonNull UserComponent userComponent) {

    }

    @Override
    protected void setupActivityComponent(ApplicationComponent applicationComponent) {
        setupUserComponent(applicationComponent);
    }

    private boolean isUserSessionStarted;
    private boolean restarted;

    @Override
    protected void hostFragment(BaseFragment fragment, int id) {
        if (!isUserSessionStarted) {
            return;
        }

        super.hostFragment(fragment, id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        restarted = savedInstanceState != null;
        super.onCreate(savedInstanceState);
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

        boolean isUserSessionStarted = getUserComponent() != null;
        if (!isUserSessionStarted) {
            Timber.e("Create UserComponent is NPE in %s. @Injected dependencies can be null - Application restarted [%s] - activities size [%s]", TAG, restarted, AppLifeCycle.activities.size());
        }
        return isUserSessionStarted;
    }

    protected UserComponent getUserComponent() {
        return AndroidApplication.instance().getUserComponent();
    }

    protected boolean isUserSessionStarted() {
        return isUserSessionStarted;
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

    public boolean clearUserSession(String message) {
        //Remove all sticky event in app
        eventBus.removeAllStickyEvents();

        if (TAG.equals(LoginZaloActivity.class.getSimpleName())) {
            return false;
        }

        getAppComponent().applicationSession().setMessageAtLogin(message);
        getAppComponent().applicationSession().clearUserSession();
        return true;
    }
}
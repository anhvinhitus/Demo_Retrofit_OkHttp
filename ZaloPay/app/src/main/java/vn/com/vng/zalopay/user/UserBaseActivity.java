package vn.com.vng.zalopay.user;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
import vn.com.vng.zalopay.event.SignOutEvent;
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

    @Override
    public void onPause() {
        super.onPause();
        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
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

    public UserComponent getUserComponent() {
        return AndroidApplication.instance().getUserComponent();
    }

    protected boolean isUserSessionStarted() {
        return isUserSessionStarted;
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onSignOut(SignOutEvent event) {
        clearUserSession(event.getMessage());
    }

    protected boolean clearUserSession(@Nullable String message) {
        mEventBus.removeAllStickyEvents();
        getAppComponent().applicationSession().setMessageAtLogin(message);
        getAppComponent().applicationSession().clearUserSession();
        return true;
    }
}
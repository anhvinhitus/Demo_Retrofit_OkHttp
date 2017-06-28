package vn.com.vng.zalopay.user;

import android.os.Bundle;
import android.support.annotation.NonNull;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.app.AppLifeCycle;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
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

        if (!isUserSessionStarted) {
            navigator.startLoginActivity(this, true);
            finish();
            return;
        }

        onUserComponentSetup(getUserComponent());
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

    protected boolean isUserSessionStarted() {
        return isUserSessionStarted;
    }
}
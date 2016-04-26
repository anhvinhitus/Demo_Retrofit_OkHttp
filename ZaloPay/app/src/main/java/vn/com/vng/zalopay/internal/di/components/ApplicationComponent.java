package vn.com.vng.zalopay.internal.di.components;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Component;
import vn.com.vng.zalopay.UserConfig;
import vn.com.vng.zalopay.internal.di.modules.ApplicationModule;
import vn.com.vng.zalopay.internal.di.modules.user.UserModule;
import vn.com.vng.zalopay.mdl.BundleService;
import vn.com.vng.zalopay.ui.activity.MiniApplicationActivity;
import vn.com.vng.zalopay.ui.activity.SplashScreenActivity;
import vn.com.vng.zalopay.ui.fragment.LoginFragment;
import vn.com.vng.zalopay.domain.executor.PostExecutionThread;
import vn.com.vng.zalopay.domain.executor.ThreadExecutor;


@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {
    //Exposed to sub-graphs.
    Context context();

    ThreadExecutor threadExecutor();

    PostExecutionThread postExecutionThread();

    UserComponent plus(UserModule userModule);

    SharedPreferences sharedPreferences();

    UserConfig userConfig();

    BundleService bundleService();

    /*INJECT*/
    void inject(LoginFragment f);
    void inject(SplashScreenActivity f);
    void inject(MiniApplicationActivity a);
}

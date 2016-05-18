package vn.com.vng.zalopay.internal.di.components;

import android.content.Context;
import android.content.SharedPreferences;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Singleton;

import dagger.Component;
import vn.com.vng.zalopay.account.ui.activities.LoginZaloActivity;
import vn.com.vng.zalopay.account.utils.ZaloProfilePreferences;
import vn.com.vng.zalopay.data.api.ParamRequestProvider;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.executor.PostExecutionThread;
import vn.com.vng.zalopay.domain.executor.ThreadExecutor;
import vn.com.vng.zalopay.domain.repository.PassportRepository;
import vn.com.vng.zalopay.internal.di.modules.ApiModule;
import vn.com.vng.zalopay.internal.di.modules.AppControllerModule;
import vn.com.vng.zalopay.internal.di.modules.ApplicationModule;
import vn.com.vng.zalopay.internal.di.modules.NetworkModule;
import vn.com.vng.zalopay.internal.di.modules.user.UserModule;
import vn.com.vng.zalopay.mdl.BundleService;
import vn.com.vng.zalopay.ui.activity.SplashScreenActivity;
import vn.com.vng.zalopay.ui.fragment.SplashScreenFragment;

@Singleton
@Component(modules = {ApplicationModule.class, NetworkModule.class, ApiModule.class, AppControllerModule.class})
public interface ApplicationComponent {
    //Exposed to sub-graphs.
    Context context();

    ThreadExecutor threadExecutor();

    PostExecutionThread postExecutionThread();

    UserComponent plus(UserModule userModule);

    EventBus eventBus();

    SharedPreferences sharedPreferences();

    UserConfig userConfig();

    BundleService bundleService();

    ZaloProfilePreferences profilePreferences();

    ParamRequestProvider paramsRequestProvider();

    PassportRepository passportRepository();

    /*INJECT*/

    void inject(SplashScreenFragment f);

    void inject(LoginZaloActivity a);
}

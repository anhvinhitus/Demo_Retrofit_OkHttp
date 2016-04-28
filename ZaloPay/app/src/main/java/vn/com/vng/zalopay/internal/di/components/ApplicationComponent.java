package vn.com.vng.zalopay.internal.di.components;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Component;


import vn.com.vng.zalopay.data.api.ParamRequestProvider;
import vn.com.vng.zalopay.data.cache.UserConfig;

import vn.com.vng.vmpay.account.ui.activities.LoginZaloActivity;
import vn.com.vng.vmpay.account.utils.ZaloProfilePreferences;


import vn.com.vng.zalopay.domain.executor.PostExecutionThread;
import vn.com.vng.zalopay.domain.executor.ThreadExecutor;
import vn.com.vng.zalopay.domain.repository.PassportRepository;
import vn.com.vng.zalopay.internal.di.modules.ApplicationModule;
import vn.com.vng.zalopay.internal.di.modules.ApiModule;
import vn.com.vng.zalopay.internal.di.modules.NetworkModule;
import vn.com.vng.zalopay.internal.di.modules.user.UserModule;
import vn.com.vng.zalopay.ui.activity.SplashScreenActivity;

@Singleton
@Component(modules = {ApplicationModule.class, NetworkModule.class, ApiModule.class})
public interface ApplicationComponent {
    //Exposed to sub-graphs.
    Context context();

    ThreadExecutor threadExecutor();

    PostExecutionThread postExecutionThread();

    UserComponent plus(UserModule userModule);

    SharedPreferences sharedPreferences();

    UserConfig userConfig();

    ZaloProfilePreferences profilePreferences();

    ParamRequestProvider paramsRequestProvider();

    PassportRepository passportRepository();

    /*INJECT*/
    void inject(SplashScreenActivity f);

    void inject(LoginZaloActivity a);
}

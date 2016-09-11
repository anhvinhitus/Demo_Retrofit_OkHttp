package vn.com.vng.zalopay.internal.di.components;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;
import okhttp3.OkHttpClient;
import vn.com.vng.zalopay.account.ui.activities.LoginZaloActivity;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.rxbus.RxBus;
import vn.com.vng.zalopay.domain.executor.PostExecutionThread;
import vn.com.vng.zalopay.domain.executor.ThreadExecutor;
import vn.com.vng.zalopay.domain.repository.ApplicationSession;
import vn.com.vng.zalopay.domain.repository.LocalResourceRepository;
import vn.com.vng.zalopay.domain.repository.PassportRepository;
import vn.com.vng.zalopay.internal.di.modules.AppApiModule;
import vn.com.vng.zalopay.internal.di.modules.AppControllerModule;
import vn.com.vng.zalopay.internal.di.modules.AppMonitorModule;
import vn.com.vng.zalopay.internal.di.modules.AppReactNativeModule;
import vn.com.vng.zalopay.internal.di.modules.ApplicationModule;
import vn.com.vng.zalopay.internal.di.modules.NetworkModule;
import vn.com.vng.zalopay.internal.di.modules.UserModule;

import com.zalopay.apploader.BundleService;

import vn.com.vng.zalopay.monitors.IMonitorReport;
import vn.com.vng.zalopay.monitors.IMonitorTiming;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.service.DownloadService;
import vn.com.vng.zalopay.service.GlobalEventHandlingService;
import vn.com.vng.zalopay.ui.dialog.PinProfileDialog;
import vn.com.vng.zalopay.ui.fragment.InvitationCodeFragment;
import vn.com.vng.zalopay.ui.fragment.SplashScreenFragment;

@Singleton
@Component(modules = {ApplicationModule.class, NetworkModule.class, AppApiModule.class, AppControllerModule.class,
        AppMonitorModule.class,
        AppReactNativeModule.class})
public interface ApplicationComponent {
    //Exposed to sub-graphs.
    Context context();

    ThreadExecutor threadExecutor();

    PostExecutionThread postExecutionThread();

    UserComponent plus(UserModule userModule);

    EventBus eventBus();

    RxBus rxBus();

    OkHttpClient okHttpClient();

    @Named("OkHttpClientTimeoutLonger")
    OkHttpClient okHttpClientTimeoutLonger();

    SharedPreferences sharedPreferences();

    Gson gson();

    UserConfig userConfig();

    BundleService bundleService();

    /*ZaloProfilePreferences profilePreferences();*/

    PassportRepository passportRepository();

    LocalResourceRepository localResourceRepository();

    IMonitorTiming monitorTiming();

    IMonitorReport monitorReport();

    GlobalEventHandlingService globalEventService();

    Navigator navigator();

    ApplicationSession applicationSession();

    /*INJECT*/

    void inject(SplashScreenFragment f);

    void inject(LoginZaloActivity a);

    void inject(DownloadService service);

    void inject(InvitationCodeFragment f);

}

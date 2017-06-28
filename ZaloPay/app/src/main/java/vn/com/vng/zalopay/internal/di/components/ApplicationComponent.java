package vn.com.vng.zalopay.internal.di.components;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.zalopay.apploader.BundleService;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;
import okhttp3.OkHttpClient;
import vn.com.vng.zalopay.passport.OnboardingFragment;
import vn.com.vng.zalopay.passport.LoginZaloActivity;
import vn.com.vng.zalopay.data.apptransidlog.ApptransidLogStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.ga.AnalyticsStore;
import vn.com.vng.zalopay.domain.executor.ThreadExecutor;
import vn.com.vng.zalopay.domain.repository.ApplicationSession;
import vn.com.vng.zalopay.domain.repository.PassportRepository;
import vn.com.vng.zalopay.internal.di.modules.AppApiModule;
import vn.com.vng.zalopay.internal.di.modules.AppControllerModule;
import vn.com.vng.zalopay.internal.di.modules.AppLocationModule;
import vn.com.vng.zalopay.internal.di.modules.AppMonitorModule;
import vn.com.vng.zalopay.internal.di.modules.AppPromotionModule;
import vn.com.vng.zalopay.internal.di.modules.AppReactNativeModule;
import vn.com.vng.zalopay.internal.di.modules.AppResourceModule;
import vn.com.vng.zalopay.internal.di.modules.AppTransIdLogModule;
import vn.com.vng.zalopay.internal.di.modules.ApplicationModule;
import vn.com.vng.zalopay.internal.di.modules.GoogleAnalyticsModule;
import vn.com.vng.zalopay.internal.di.modules.NetworkModule;
import vn.com.vng.zalopay.internal.di.modules.UserModule;
import vn.com.vng.zalopay.location.LocationStore;
import vn.com.vng.zalopay.monitors.IMonitorTiming;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.service.DownloadService;
import vn.com.vng.zalopay.service.GlobalEventHandlingService;
import vn.com.vng.zalopay.share.IntentHandlerActivity;
import vn.com.vng.zalopay.tracker.GoogleReporter;
import vn.com.vng.zalopay.ui.activity.ExternalCallSplashScreenActivity;
import vn.com.vng.zalopay.ui.fragment.InvitationCodeFragment;
import vn.com.vng.zalopay.ui.fragment.SplashScreenFragment;

@Singleton
@Component(
        modules = {
                ApplicationModule.class,
                NetworkModule.class,
                AppApiModule.class,
                AppControllerModule.class,
                AppMonitorModule.class,
                AppReactNativeModule.class,
                AppResourceModule.class,
                AppTransIdLogModule.class,
                AppLocationModule.class,
                AppPromotionModule.class,
                GoogleAnalyticsModule.class
        }
)
public interface ApplicationComponent {
    //Exposed to sub-graphs.
    Context context();

    ThreadExecutor threadExecutor();

    UserComponent plus(UserModule userModule);

    EventBus eventBus();

    OkHttpClient okHttpClient();

    @Named("OkHttpClientTimeoutLonger")
    OkHttpClient okHttpClientTimeoutLonger();

    AnalyticsStore.Repository analyticsRepository();

    GoogleReporter googleReporter();

    SharedPreferences sharedPreferences();

    Gson gson();

    UserConfig userConfig();

    BundleService bundleService();

    PassportRepository passportRepository();

    IMonitorTiming monitorTiming();

    GlobalEventHandlingService globalEventService();

    Navigator navigator();

    ApplicationSession applicationSession();

    ApptransidLogStore.Repository appTransIdLogRepository();

    LocationStore.RepositoryFactory locationRepositoryFactory();

    LocationStore.Repository locationRepository();

    /*INJECT*/

    void inject(SplashScreenFragment f);

    void inject(OnboardingFragment f);

    void inject(DownloadService service);

    void inject(InvitationCodeFragment f);

    void inject(LoginZaloActivity activity);

    void inject(ExternalCallSplashScreenActivity activity);

    void inject(IntentHandlerActivity activity);


}

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
import vn.com.vng.zalopay.account.ui.activities.LoginZaloActivity;
import vn.com.vng.zalopay.app.ApplicationState;
import vn.com.vng.zalopay.data.apptransidlog.ApptransidLogStore;
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
import vn.com.vng.zalopay.internal.di.modules.AppResourceModule;
import vn.com.vng.zalopay.internal.di.modules.AppTransIdLogModule;
import vn.com.vng.zalopay.internal.di.modules.ApplicationModule;
import vn.com.vng.zalopay.internal.di.modules.NetworkModule;
import vn.com.vng.zalopay.internal.di.modules.UserModule;
import vn.com.vng.zalopay.monitors.IMonitorReport;
import vn.com.vng.zalopay.monitors.IMonitorTiming;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.requestsupport.ChooseCategoryFragment;
import vn.com.vng.zalopay.service.DownloadService;
import vn.com.vng.zalopay.service.GlobalEventHandlingService;
import vn.com.vng.zalopay.share.IntentHandlerActivity;
import vn.com.vng.zalopay.ui.activity.ExternalCallSplashScreenActivity;
import vn.com.vng.zalopay.ui.fragment.IntroAppFragment;
import vn.com.vng.zalopay.ui.fragment.InvitationCodeFragment;
import vn.com.vng.zalopay.ui.fragment.SplashScreenFragment;
import vn.com.vng.zalopay.utils.ImageLoader;

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
                AppTransIdLogModule.class
        }
)
public interface ApplicationComponent {
    //Exposed to sub-graphs.
    Context context();

    ThreadExecutor threadExecutor();

    PostExecutionThread postExecutionThread();

    UserComponent plus(UserModule userModule);

    ImageLoader imageLoader();

    EventBus eventBus();

    RxBus rxBus();

    OkHttpClient okHttpClient();

    @Named("OkHttpClientTimeoutLonger")
    OkHttpClient okHttpClientTimeoutLonger();

    SharedPreferences sharedPreferences();

    Gson gson();

    UserConfig userConfig();

    BundleService bundleService();

    PassportRepository passportRepository();

    LocalResourceRepository localResourceRepository();

    IMonitorTiming monitorTiming();

    IMonitorReport monitorReport();

    GlobalEventHandlingService globalEventService();

    Navigator navigator();

    ApplicationSession applicationSession();

    ApplicationState applicationState();

    ApptransidLogStore.Repository appTransIdLogRepository();

    /*INJECT*/

    void inject(SplashScreenFragment f);

    void inject(LoginZaloActivity a);

    void inject(DownloadService service);

    void inject(InvitationCodeFragment f);

    void inject(IntroAppFragment f);

    void inject(ExternalCallSplashScreenActivity a);

    void inject(ChooseCategoryFragment a);

    void inject(IntentHandlerActivity a);
}

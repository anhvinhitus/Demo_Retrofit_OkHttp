package vn.com.vng.zalopay;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.react.modules.fresco.FrescoModule;
import com.facebook.react.views.text.ReactFontManager;
import com.frogermcs.androiddevmetrics.AndroidDevMetrics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.squareup.leakcanary.LeakCanary;
import com.zalopay.apploader.logging.ReactNativeAppLoaderLogger;
import com.zalopay.ui.widget.iconfont.IconFontHelper;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;
import com.zing.zalo.zalosdk.oauth.ZaloSDKApplication;

import org.greenrobot.eventbus.EventBus;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;
import vn.com.vng.zalopay.app.AppLifeCycle;
import vn.com.vng.zalopay.data.appresources.ResourceHelper;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.event.LoadIconFontEvent;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.internal.di.components.DaggerApplicationComponent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.internal.di.modules.ApplicationModule;
import vn.com.vng.zalopay.internal.di.modules.UserModule;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;
import vn.com.vng.zalopay.service.ZPTrackerAnswers;
import vn.com.vng.zalopay.service.ZPTrackerApptransid;
import vn.com.vng.zalopay.service.ZPTrackerGA;
import vn.com.vng.zalopay.utils.ConfigUtil;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.wallet.configure.SDKConfiguration;
import vn.com.zalopay.wallet.controller.SDKApplication;

/**
 * Created by AnhHieu on 3/24/16.
 * *
 */
public class AndroidApplication extends Application {

    private ApplicationComponent appComponent;
    private UserComponent userComponent;

    private static AndroidApplication _instance;

    public static AndroidApplication instance() {
        return _instance;
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (BuildConfig.DEBUG) {
            MultiDex.install(this);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        _instance = this;

        registerActivityLifecycleCallbacks(new AppLifeCycle());

        FLog.setLoggingDelegate(new ReactNativeAppLoaderLogger());
        if (BuildConfig.DEBUG) {
            FLog.setMinimumLoggingLevel(Log.INFO);
            Timber.plant(new Timber.DebugTree());
            AndroidDevMetrics.initWith(this);
            StrictMode.enableDefaults();
            LeakCanary.install(this);
        } else {
            FLog.setMinimumLoggingLevel(Log.ERROR);
            Timber.plant(new CrashlyticsTree());
        }

        Fabric.with(this, new Crashlytics());
        initializeFresco();


        initAppComponent();

        initializeZaloPayAnalytics();

        Timber.d("onCreate %s", appComponent);
        ZaloSDKApplication.wrap(this);
        if (!BuildConfig.DEBUG) {
            // Disable Zalo log on RELEASE
            ZaloSDK.Instance.setLogLevel(Log.WARN);
        }

        initPaymentSdk();

        Thread.setDefaultUncaughtExceptionHandler(appComponent.globalEventService());

        initConfig();
        initIconFont();
    }

    private void initConfig() {
        ConfigUtil.initConfig(getAssets());
    }

    public void initIconFont() {
        IconFontHelper.getInstance().initialize(getAssets(),
                "fonts/" + getString(R.string.font_name),
                "fonts/" + getString(R.string.json_font_info),
                ResourceHelper.getFontPath(BuildConfig.ZALOPAY_APP_ID) + getString(R.string.font_name),
                ResourceHelper.getFontPath(BuildConfig.ZALOPAY_APP_ID) + getString(R.string.json_font_info));
        if (IconFontHelper.getInstance().getCurrentTypeface() != null) {
            ReactFontManager.getInstance().setTypeface(PaymentAppConfig.Constants.FONT_FAMILY_NAME_ZALOPAY,
                    Typeface.NORMAL,
                    IconFontHelper.getInstance().getCurrentTypeface());
        }
        EventBus.getDefault().postSticky(new LoadIconFontEvent(IconFontHelper.getInstance().getCurrentIconFontType()));
    }

    private void initPaymentSdk() {
        SDKConfiguration sdkConfig = SDKConfiguration.newBuilder()
                .setHttpClientTimeoutLonger(getAppComponent().okHttpClientTimeoutLonger())
                .setReleaseBuild(!BuildConfig.DEBUG)
                .setBaseHostUrl(BuildConfig.HOST)
                .build();
        SDKApplication.initialize(this,sdkConfig);
    }

    private void initializeZaloPayAnalytics() {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
        final Tracker tracker = analytics.newTracker(BuildConfig.GA_Tracker);

        ZPAnalytics.addDefaultTracker();
        ZPAnalytics.addTracker(new ZPTrackerGA(tracker));
        ZPAnalytics.addTracker(new ZPTrackerAnswers());
        ZPAnalytics.addTracker(new ZPTrackerApptransid(appComponent.appTransIdLogRepository()));
    }

    private void initializeFresco() {
        // if (!Fresco.hasBeenInitialized()) {
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(this, config);
        FrescoModule.sHasBeenInitialized = true;
        //   }
    }


    private void initAppComponent() {
        appComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();

        appComponent.userConfig().loadConfig();
        appComponent.threadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                appComponent.bundleService().ensureLocalResources();
            }
        });
    }

    /*private void initializeFontFamily() {
        AndroidUtils.setDefaultFont(this, "DEFAULT", "fonts/Roboto-Regular.ttf");
        AndroidUtils.setDefaultFont(this, "DEFAULT_BOLD", "fonts/Roboto-Medium.ttf");
        AndroidUtils.setDefaultFont(this, "MONOSPACE", "fonts/Roboto-Medium.ttf");
        AndroidUtils.setDefaultFont(this, "SERIF", "fonts/Roboto-Regular.ttf");
        AndroidUtils.setDefaultFont(this, "SANS_SERIF", "fonts/Roboto-Regular.ttf");
    }*/

    public UserComponent createUserComponent(User user) {
        Timber.d("Create new instance of UserComponent");
        userComponent = appComponent.plus(new UserModule(user));
        return userComponent;
    }

    public void releaseUserComponent() {
        Timber.d("Release instance of UserComponent");
        if (userComponent != null) {
            userComponent.reactNativeInstanceManager().cleanup();
        }
        userComponent = null;
    }

    public ApplicationComponent getAppComponent() {
        return appComponent;
    }

    @Nullable
    public UserComponent getUserComponent() {
        return userComponent;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Fresco.getImagePipeline().clearMemoryCaches();
    }

}

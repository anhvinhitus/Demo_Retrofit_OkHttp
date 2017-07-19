package vn.com.vng.zalopay;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.react.modules.fresco.FrescoModule;
import com.facebook.react.views.text.ReactFontManager;
import com.frogermcs.androiddevmetrics.AndroidDevMetrics;
import com.squareup.leakcanary.LeakCanary;
import com.zalopay.apploader.logging.ReactNativeAppLoaderLogger;
import com.zalopay.ui.widget.util.FontLoader;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;
import com.zing.zalo.zalosdk.oauth.ZaloSDKApplication;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;
import vn.com.vng.zalopay.app.AppLifeCycle;
import vn.com.vng.zalopay.data.appresources.ResourceHelper;
import vn.com.vng.zalopay.data.util.ConfigLoader;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.internal.di.components.DaggerApplicationComponent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.internal.di.modules.ApplicationModule;
import vn.com.vng.zalopay.internal.di.modules.UserModule;
import vn.com.vng.zalopay.location.LocationProvider;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;
import vn.com.vng.zalopay.tracker.ZPTrackerAnswers;
import vn.com.vng.zalopay.tracker.ZPTrackerApptransid;
import vn.com.vng.zalopay.tracker.ZPTrackerFileAppender;
import vn.com.vng.zalopay.tracker.ZPTrackerGA;
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
            if (LeakCanary.isInAnalyzerProcess(this)) {
                return;
            }

            FLog.setMinimumLoggingLevel(Log.INFO);
            Timber.plant(new Timber.DebugTree());
            AndroidDevMetrics.initWith(this);
            StrictMode.enableDefaults();

            LeakCanary.install(this);
        } else {
            FLog.setMinimumLoggingLevel(Log.ERROR);
            Timber.plant(new CrashlyticsTree());
        }

        FontLoader.initialize(this);

        initializeFresco();

        ResourceHelper.initialize(this, BuildConfig.DEBUG);

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

        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();
        Fabric.with(this, crashlyticsKit);

        ConfigLoader.initConfig(getAssets(), BuildConfig.WITHDRAW_APP_ID);
        LocationProvider.init(appComponent.locationRepositoryFactory(), this);

    }

    private void backgroundInitialization() {
        appComponent.bundleService().ensureLocalResources();
        loadFontFromApp1();
    }

    public void loadFontFromApp1() {
        String fontPath = ResourceHelper.getFontPath(BuildConfig.ZALOPAY_APP_ID) + getString(R.string.font_name);
        String codePath = ResourceHelper.getFontPath(BuildConfig.ZALOPAY_APP_ID) + getString(R.string.json_font_info);
        boolean isLoadFontSuccess = FontLoader.loadFont(fontPath, codePath);

        if (isLoadFontSuccess) {
            return;
        }

        Typeface typeface = FontLoader.getDefaultTypeface();
        ReactFontManager.getInstance()
                .setTypeface(PaymentAppConfig.Constants.FONT_FAMILY_NAME_ZALOPAY,
                        Typeface.NORMAL, typeface);
    }

    private void initPaymentSdk() {
        SDKConfiguration sdkConfig = SDKConfiguration.newBuilder()
                .setHttpClientTimeoutLonger(getAppComponent().okHttpClientTimeoutLonger())
                .setReleaseBuild(!BuildConfig.DEBUG)
                .setBaseHostUrl(BuildConfig.HOST)
                .setRetrofit(getAppComponent().retrofitApi())
                .build();
        SDKApplication.initialize(this, sdkConfig);
    }

    private void initializeZaloPayAnalytics() {
        ZPAnalytics.addDefaultTracker();
        ZPAnalytics.addTracker(new ZPTrackerGA(appComponent.googleReporter()));
        ZPAnalytics.addTracker(new ZPTrackerAnswers());
        ZPAnalytics.addTracker(new ZPTrackerFileAppender());
        ZPAnalytics.addTracker(new ZPTrackerApptransid(appComponent.appTransIdLogRepository()));
    }

    private void initializeFresco() {
        if (!Fresco.hasBeenInitialized()) {
            ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                    .setDownsampleEnabled(true)
                    .build();
            Fresco.initialize(this, config);
            FrescoModule.sHasBeenInitialized = true;
        }
    }

    private void initAppComponent() {
        appComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();

        appComponent.userConfig().loadConfig();
        appComponent.threadExecutor().execute(this::backgroundInitialization);
    }

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

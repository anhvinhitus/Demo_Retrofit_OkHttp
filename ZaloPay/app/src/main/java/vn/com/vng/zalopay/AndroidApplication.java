package vn.com.vng.zalopay;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.frogermcs.androiddevmetrics.AndroidDevMetrics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.squareup.leakcanary.LeakCanary;
import com.zalopay.apploader.logging.ReactNativeAppLoaderLogger;
import com.zing.zalo.zalosdk.oauth.ZaloSDKApplication;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;
import vn.com.vng.debugviewer.DebugViewer;
import vn.com.vng.zalopay.app.AppLifeCycle;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.exception.InvitationCodeException;
import vn.com.vng.zalopay.data.exception.NetworkConnectionException;
import vn.com.vng.zalopay.data.exception.TokenException;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.internal.di.components.DaggerApplicationComponent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.internal.di.modules.ApplicationModule;
import vn.com.vng.zalopay.internal.di.modules.UserModule;
import vn.com.vng.zalopay.service.ZPTrackerAnswers;
import vn.com.vng.zalopay.service.ZPTrackerGA;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.controller.WalletSDKApplication;
import vn.com.zalopay.wallet.business.data.Constants;

/**
 * Created by AnhHieu on 3/24/16.
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

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            AndroidDevMetrics.initWith(this);
            StrictMode.enableDefaults();
            LeakCanary.install(this);
            DebugViewer.registerInstance(this);
            Timber.plant(new Timber.DebugTree() {
                @Override
                protected void log(int priority, String tag, String message, Throwable t) {
                    DebugViewer.postLog(priority, tag, message);
                }
            });
        } else {
            Timber.plant(new CrashlyticsTree());
        }

        FLog.setLoggingDelegate(ReactNativeAppLoaderLogger.getInstance());

        Fabric.with(this, new Crashlytics());
        initializeFresco();

        // Initialize ZPAnalytics
        initializeZaloPayAnalytics();

        initAppComponent();

        Timber.d(" onCreate " + appComponent);
        ZaloSDKApplication.wrap(this);

        initPaymentSdk();

        Thread.setDefaultUncaughtExceptionHandler(appComponent.globalEventService());
        ZPAnalytics.trackEvent(ZPEvents.APP_LAUNCH);

    }

    private void initPaymentSdk() {
        WalletSDKApplication.wrap(this);
        WalletSDKApplication.setHttpClient(getAppComponent().okHttpClient());
        WalletSDKApplication.setHttpClientTimeoutLonger(getAppComponent().okHttpClientTimeoutLonger());
        Constants.IS_RELEASE = !BuildConfig.DEBUG;
        Constants.setEnumEnvironment(BuildConfig.HOST_TYPE);
    }

    private void initializeZaloPayAnalytics() {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
        final Tracker tracker = analytics.newTracker(R.xml.global_tracker);

        ZPAnalytics.addDefaultTracker();
        ZPAnalytics.addTracker(new ZPTrackerGA(tracker));
        ZPAnalytics.addTracker(new ZPTrackerAnswers());
    }

    void initializeFresco() {
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(this, config);
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

    private void initializeFontFamily() {
        AndroidUtils.setDefaultFont(this, "DEFAULT", "fonts/Roboto-Regular.ttf");
        AndroidUtils.setDefaultFont(this, "DEFAULT_BOLD", "fonts/Roboto-Medium.ttf");
        AndroidUtils.setDefaultFont(this, "MONOSPACE", "fonts/Roboto-Medium.ttf");
        AndroidUtils.setDefaultFont(this, "SERIF", "fonts/Roboto-Regular.ttf");
        AndroidUtils.setDefaultFont(this, "SANS_SERIF", "fonts/Roboto-Regular.ttf");
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

    public UserComponent getUserComponent() {
        return userComponent;
    }

    public class CrashlyticsTree extends Timber.Tree {
        private static final String CRASHLYTICS_KEY_PRIORITY = "priority";
        private static final String CRASHLYTICS_KEY_TAG = "tag";
        private static final String CRASHLYTICS_KEY_MESSAGE = "message";

        @Override
        protected void log(int priority, @Nullable String tag, @Nullable String message, @Nullable Throwable t) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
                return;
            }

            if (t instanceof InvitationCodeException
                    || t instanceof BodyException
                    || t instanceof TokenException
                    || t instanceof NetworkConnectionException) {
                return;
            }

            Crashlytics.setInt(CRASHLYTICS_KEY_PRIORITY, priority);
            Crashlytics.setString(CRASHLYTICS_KEY_TAG, tag);
            Crashlytics.setString(CRASHLYTICS_KEY_MESSAGE, message);

            if (t == null) {
                Crashlytics.logException(new Exception(message));
            } else {
                Crashlytics.logException(t);
            }
        }
    }
}

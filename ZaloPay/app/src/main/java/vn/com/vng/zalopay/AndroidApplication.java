package vn.com.vng.zalopay;

import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.frogermcs.androiddevmetrics.AndroidDevMetrics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.squareup.leakcanary.LeakCanary;
import com.zing.zalo.zalosdk.oauth.ZaloSDKApplication;

import io.fabric.sdk.android.Fabric;
import io.netty.util.internal.logging.InternalLoggerFactory;
import timber.log.Timber;
import vn.com.vng.debugviewer.DebugViewer;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.vng.zalopay.app.AppLifeCycle;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.exception.InvitationCodeException;
import vn.com.vng.zalopay.data.exception.NetworkConnectionException;
import vn.com.vng.zalopay.data.exception.TokenException;
import vn.com.vng.zalopay.data.ws.logger.NonLoggerFactory;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.internal.di.components.DaggerApplicationComponent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.internal.di.modules.ApplicationModule;
import vn.com.vng.zalopay.internal.di.modules.UserModule;
import vn.com.vng.zalopay.service.ZPTrackerAnswers;
import vn.com.vng.zalopay.service.ZPTrackerGA;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.zalopay.wallet.application.ZingMobilePayApplication;
import vn.com.zalopay.wallet.data.Constants;

/**
 * Created by AnhHieu on 3/24/16.
 */
public class AndroidApplication extends MultiDexApplication {

    private ApplicationComponent appComponent;
    private UserComponent userComponent;

    private static AndroidApplication _instance;

    public static AndroidApplication instance() {
        return _instance;
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
            InternalLoggerFactory.setDefaultFactory(new NonLoggerFactory());
        }

        Fabric.with(this, new Crashlytics());

        // Initialize ZPAnalytics
        initializeZaloPayAnalytics();

        initAppComponent();

        Timber.d(" onCreate " + appComponent);
        ZaloSDKApplication.wrap(this);
        ZingMobilePayApplication.wrap(this);
        Constants.IS_RELEASE = !BuildConfig.DEBUG;
        // Constants.setUrlPrefix(BuildConfig.HOST_TYPE);
        Constants.setEnumEnvironment(BuildConfig.HOST_TYPE);

        Thread.setDefaultUncaughtExceptionHandler(appComponent.globalEventService());
        ZPAnalytics.trackEvent(ZPEvents.APP_LAUNCH);
    }

    private void initializeZaloPayAnalytics() {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
        final Tracker tracker = analytics.newTracker(R.xml.global_tracker);

        ZPAnalytics.addDefaultTracker();
        ZPAnalytics.addTracker(new ZPTrackerGA(tracker));
        ZPAnalytics.addTracker(new ZPTrackerAnswers());
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
        // AndroidUtils.setDefaultFont(this, "MONOSPACE", "MyFontAsset2.ttf");
        // AndroidUtils.setDefaultFont(this, "SERIF", "MyFontAsset3.ttf");
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

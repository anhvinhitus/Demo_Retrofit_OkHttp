package vn.com.vng.zalopay;

import android.os.Environment;
import android.os.StrictMode;
import android.support.multidex.MultiDexApplication;

import com.frogermcs.androiddevmetrics.AndroidDevMetrics;
import com.squareup.leakcanary.LeakCanary;
import com.zing.zalo.zalosdk.oauth.ZaloSDKApplication;

import java.io.File;

import timber.log.Timber;
import vn.com.vng.iot.debugviewer.DebugViewer;
import vn.com.vng.zalopay.app.AppLifeCycle;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.internal.di.components.DaggerApplicationComponent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.internal.di.modules.ApplicationModule;
import vn.com.vng.zalopay.internal.di.modules.user.UserModule;
import vn.com.zalopay.wallet.ZingMobilePayApplication;
import vn.com.zalopay.wallet.data.Constants;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

/**
 * Created by AnhHieu on 3/24/16.
 *
 */
public class AndroidApplication extends MultiDexApplication {

    public static final String TAG = "AndroidApplication";

    public static File extStorageAppBasePath;
    public static File extStorageAppCachePath;


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
            Timber.tag(TAG);
            AndroidDevMetrics.initWith(this);
            StrictMode.enableDefaults();
            LeakCanary.install(this);
            DebugViewer.registerInstance(this);
            Timber.plant(new Timber.Tree() {
                @Override
                protected void log(int priority, String tag, String message, Throwable t) {
                    DebugViewer.postLog(priority, tag, message);
                }
            });
        }

        Fabric.with(this, new Crashlytics());

        initAppComponent();
        initializeFileFolder();

        Timber.d(" onCreate " + appComponent);
        ZaloSDKApplication.wrap(this);
        ZingMobilePayApplication.wrap(this);
        Constants.IS_RELEASE = BuildConfig.ENV_LIVE;
        Constants.setUrlPrefix(BuildConfig.HOST_TYPE);

        if (BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable throwable) {
                    Timber.e(throwable, "UncaughtException!!!");
                }
            });
        }
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
//                appComponent.bundleService().prepareInternalBundle();
//                appComponent.bundleService().extractAllExternalApplication();
            }
        });
    }

    public UserComponent createUserComponent(User user) {
        userComponent = appComponent.plus(new UserModule(user));
        return userComponent;
    }

    public void releaseUserComponent() {
        userComponent = null;
    }

    public ApplicationComponent getAppComponent() {
        return appComponent;
    }

    public UserComponent getUserComponent() {
        return userComponent;
    }

    private void initializeFileFolder() {
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            File externalStorageDir = Environment.getExternalStorageDirectory();

            if (externalStorageDir != null) {
                extStorageAppBasePath = new File(
                        externalStorageDir.getAbsolutePath() + File.separator
                                + "Android" + File.separator + "data"
                                + File.separator + getPackageName());
            }

            if (extStorageAppBasePath != null) {
                extStorageAppCachePath = new File(
                        extStorageAppBasePath.getAbsolutePath()
                                + File.separator + "cache");

                boolean isCachePathAvailable = true;

                if (!extStorageAppCachePath.exists()) {
                    isCachePathAvailable = extStorageAppCachePath.mkdirs();
                }

                if (!isCachePathAvailable) {
                    extStorageAppCachePath = null;
                }
            }

        }
    }
}

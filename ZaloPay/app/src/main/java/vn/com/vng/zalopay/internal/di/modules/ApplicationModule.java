package vn.com.vng.zalopay.internal.di.modules;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.preference.PreferenceManager;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.UIThread;
import vn.com.vng.zalopay.UserConfigImpl;
import vn.com.vng.zalopay.analytics.ZPAnalytics;
import vn.com.vng.zalopay.analytics.ZPTracker;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.cache.helper.DBOpenHelper;
import vn.com.vng.zalopay.data.cache.model.DaoMaster;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.appresources.DownloadAppResourceTaskQueue;
import vn.com.vng.zalopay.data.executor.JobExecutor;
import vn.com.vng.zalopay.domain.executor.PostExecutionThread;
import vn.com.vng.zalopay.domain.executor.ThreadExecutor;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.service.ApplicationSession;
import vn.com.vng.zalopay.service.DownloadService;
import vn.com.vng.zalopay.service.GlobalEventHandlingService;
import vn.com.vng.zalopay.service.GlobalEventHandlingServiceImpl;
import vn.com.vng.zalopay.service.ZPTrackerGA;
import vn.com.vng.zalopay.utils.AndroidUtils;


@Module
public class ApplicationModule {
    private final AndroidApplication application;

    public ApplicationModule(AndroidApplication application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Context provideApplicationContext() {
        return this.application;
    }

    @Provides
    @Singleton
    ThreadExecutor provideThreadExecutor(JobExecutor jobExecutor) {
        return jobExecutor;
    }

    @Provides
    @Singleton
    PostExecutionThread providePostExecutionThread(UIThread uiThread) {
        return uiThread;
    }

    @Provides
    @Singleton
    EventBus providesEventBus() {
        return EventBus.getDefault();
    }

    @Provides
    @Singleton
    SharedPreferences providesSharedPreferences(Context application) {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Provides
    @Singleton
    UserConfig providesUserConfig(@Named("daosession") DaoSession daoSession, SharedPreferences sharedPreferences, EventBus eventBus) {
        return new UserConfigImpl(daoSession, sharedPreferences, eventBus);
    }

    @Provides
    @Singleton
    Navigator provideNavigator(UserConfig userConfig) {
        return new Navigator(userConfig);
    }

    @Provides
    @Singleton
    @Named("daosession")
    DaoSession provideDaoSession(Context context) {
        DaoMaster.OpenHelper helper = new DBOpenHelper(context, "zalopay.db");
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        return daoMaster.newSession();
    }


    @Provides
    @Singleton
    @Named("payAppId")
    int providesPayAppId() {
        return BuildConfig.PAYAPPID;
    }

    @Provides
    @Singleton
    @Named("devicemodel")
    String providesDeviceModel() {
        return Build.MODEL;
    }

    @Provides
    @Singleton
    @Named("platformcode")
    String providesPlatformcode() {
        return "android";
    }


    @Provides
    @Singleton
    @Named("params_request_default")
    HashMap<String, String> providesParamsRequest() {
        HashMap<String, String> params = new HashMap<>();
        params.put("platformcode", "android");
        params.put("dscreentype", AndroidUtils.getScreenType()); //ldpi, dpi, mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi"
        params.put("devicemodel", Build.MODEL);
        return params;
    }


    @Provides
    @Singleton
    DownloadAppResourceTaskQueue providesDownloadAppResource(Context context) {
        return DownloadAppResourceTaskQueue.create(context, DownloadService.class);
    }


    @Provides
    @Singleton
    GlobalEventHandlingService providesGlobalEventService(EventBus eventBus) {
        return new GlobalEventHandlingServiceImpl(eventBus);
    }

    @Provides
    @Singleton
    ApplicationSession providesApplicationSession(Context context, Navigator navigator) {
        return new ApplicationSession(context, navigator);
    }

    @Provides
    @Singleton
    Tracker provideDefaultTracker() {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(application);
        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
        return analytics.newTracker(R.xml.global_tracker);

    }

    @Provides
    @Singleton
    ZPAnalytics provideZPAnalytics() {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(application);
        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
        final Tracker tracker = analytics.newTracker(R.xml.global_tracker);

        ZPAnalytics.Builder builder = new ZPAnalytics.Builder();
        builder.addDefaultTracker();
        builder.addTracker(new ZPTrackerGA(tracker, application));
        return builder.build();
    }
}

package vn.com.zalopay.wallet.di.module;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.data.cache.MemoryCache;
import vn.com.vng.zalopay.data.cache.MemoryCacheLru;
import vn.com.vng.zalopay.monitors.ZPMonitorEventTiming;
import vn.com.vng.zalopay.monitors.ZPMonitorEventTimingDefault;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;

@Singleton
@Module
public class ApplicationModule {
    protected Application mApplication;

    public ApplicationModule(Application pApp) {
        this.mApplication = pApp;
    }

    @Provides
    @Singleton
    public Application provideApplication() {
        return mApplication;
    }

    @Provides
    @Singleton
    public Resources provideResources() {
        return mApplication.getResources();
    }

    @Provides
    @Singleton
    public Context providerContext() {
        return mApplication.getApplicationContext();
    }

    @Provides
    @Singleton
    public SharedPreferencesManager providerSharePreferenceManager(Context pContext) {
        return SharedPreferencesManager.shared(pContext);
    }

    @Provides
    @Singleton
    public EventBus provideEventBus() {
        return EventBus.getDefault();
    }

    @Provides
    @Singleton
    public ZPMonitorEventTiming provideEventTiming() {
        return new ZPMonitorEventTimingDefault();
    }

    @Provides
    @Singleton
    MemoryCache provideMemoryCache() {
        return new MemoryCacheLru();
    }
}

package vn.com.vng.zalopay.internal.di.modules;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.UIThread;
import vn.com.vng.zalopay.UserConfigImpl;
import vn.com.vng.zalopay.data.api.ParamRequestProvider;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.executor.JobExecutor;
import vn.com.vng.zalopay.data.repository.PassportRepositoryImpl;
import vn.com.vng.zalopay.domain.executor.PostExecutionThread;
import vn.com.vng.zalopay.domain.executor.ThreadExecutor;
import vn.com.vng.zalopay.domain.repository.PassportRepository;


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
    SharedPreferences providesSharedPreferences(Context application) {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Provides
    @Singleton
    PassportRepository providePassportRepository(PassportRepositoryImpl passportRepository) {
        return passportRepository;
    }

    @Provides
    @Singleton
    @Named("param_provider")
    ParamRequestProvider provideParamRequestProvider() {
        return new ParamRequestProvider();
    }

    @Provides
    @Singleton
    UserConfig providesUserConfig(SharedPreferences sharedPreferences) {
        return new UserConfigImpl(sharedPreferences);
    }

}

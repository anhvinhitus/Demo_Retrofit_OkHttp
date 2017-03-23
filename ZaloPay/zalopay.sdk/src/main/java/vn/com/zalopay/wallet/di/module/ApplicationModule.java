package vn.com.zalopay.wallet.di.module;

import android.app.Application;
import android.content.res.Resources;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
    protected Application mApplication;

    public AppModule(Application pApp) {
        this.mApplication = pApp;
    }

    @Provides
    @Singleton
    public Application provideApplication() {
        return mApplication;
    }

    @Provides
    @Singleton
    protected Resources provideResources() {

        return mApplication.getResources();

    }
}

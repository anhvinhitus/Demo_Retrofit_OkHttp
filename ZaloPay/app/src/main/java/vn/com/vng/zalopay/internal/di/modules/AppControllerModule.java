package vn.com.vng.zalopay.internal.di.modules;

import android.app.Application;
import android.content.Context;

import com.google.gson.Gson;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.repository.PassportRepositoryImpl;
import vn.com.vng.zalopay.data.repository.datasource.UserConfigFactory;
import vn.com.vng.zalopay.domain.repository.PassportRepository;
import vn.com.vng.zalopay.mdl.BundleService;
import vn.com.vng.zalopay.mdl.impl.BundleServiceImpl;

/**
 * Created by AnhHieu on 4/2/16.
 */

@Module
public class AppControllerModule {

    public AppControllerModule() {
    }

    @Provides
    @Singleton
    PassportRepository providePassportRepository(PassportRepositoryImpl passportRepository) {
        return passportRepository;
    }

    @Provides
    @Singleton
    BundleService providesBundleService(Context context, Gson gson) {
        return new BundleServiceImpl((Application) context, gson);
    }

    @Provides
    @Singleton
    UserConfigFactory provideUserConfigFactory(Context context, UserConfig userConfig, @Named("daosession") DaoSession daoSession) {
        return new UserConfigFactory(context, userConfig, daoSession);
    }
}

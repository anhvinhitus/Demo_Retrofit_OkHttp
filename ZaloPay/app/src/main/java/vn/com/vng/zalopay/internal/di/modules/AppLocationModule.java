package vn.com.vng.zalopay.internal.di.modules;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.data.cache.AppStorage;
import vn.com.vng.zalopay.data.cache.AppStorageImpl;
import vn.com.vng.zalopay.data.cache.global.DaoSession;
import vn.com.vng.zalopay.location.LocationLocalStorage;
import vn.com.vng.zalopay.location.LocationRepository;
import vn.com.vng.zalopay.location.LocationStore;

/**
 * Created by khattn on 3/22/17.
 * Module of location db
 */

@Module
public class AppLocationModule {
    @Singleton
    @Provides
    AppStorage provideAppStorage(@Named("globaldaosession") DaoSession session) {
        return new AppStorageImpl(session);
    }

    @Singleton
    @Provides
    LocationStore.LocalStorage provideLocationLocalStorage(AppStorage appStorage) {
        return new LocationLocalStorage(appStorage);
    }

    @Singleton
    @Provides
    LocationStore.Repository provideLocationRepository(LocationStore.LocalStorage localStorage) {
        return new LocationRepository(localStorage);
    }
}

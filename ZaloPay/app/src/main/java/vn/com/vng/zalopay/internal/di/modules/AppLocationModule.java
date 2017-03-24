package vn.com.vng.zalopay.internal.di.modules;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.data.cache.global.DaoSession;
import vn.com.vng.zalopay.location.LocationDataMapper;
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
    LocationStore.LocalStorage provideLocationLocalStorage(@Named("globaldaosession") DaoSession session) {
        return new LocationLocalStorage(session);
    }

    @Singleton
    @Provides
    LocationStore.Repository provideLocationRepository(LocationStore.LocalStorage localStorage,
                                                       LocationDataMapper mapper) {
        return new LocationRepository(localStorage, mapper);
    }
}

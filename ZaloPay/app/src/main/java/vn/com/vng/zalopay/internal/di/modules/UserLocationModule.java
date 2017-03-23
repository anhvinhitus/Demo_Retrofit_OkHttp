package vn.com.vng.zalopay.internal.di.modules;

import com.google.gson.Gson;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.location.LocationLocalStorage;
import vn.com.vng.zalopay.location.LocationRepository;
import vn.com.vng.zalopay.location.LocationStore;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.internal.di.scope.UserScope;

/**
 * Created by khattn on 3/22/17.
 *
 */

@Module
public class UserLocationModule {

    @UserScope
    @Provides
    LocationStore.Repository providesLocationRepository(@Named("daosession") DaoSession session, Gson gson) {
        return new LocationRepository(new LocationLocalStorage(session, gson));
    }
}

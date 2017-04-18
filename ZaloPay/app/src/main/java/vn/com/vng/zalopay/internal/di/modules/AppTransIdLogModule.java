package vn.com.vng.zalopay.internal.di.modules;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.data.api.entity.mapper.ApptransidLogEntityDataMapper;
import vn.com.vng.zalopay.data.apptransidlog.ApptransidLogLocalStorage;
import vn.com.vng.zalopay.data.apptransidlog.ApptransidLogRepository;
import vn.com.vng.zalopay.data.apptransidlog.ApptransidLogStore;
import vn.com.vng.zalopay.data.cache.global.DaoSession;

/**
 * Created by khattn on 1/24/17.
 */

@Module
public class AppTransIdLogModule {

    @Singleton
    @Provides
    ApptransidLogStore.LocalStorage provideApptransidLogLocalStorage(@Named("globaldaosession") DaoSession session) {
        return new ApptransidLogLocalStorage(session);
    }

    @Singleton
    @Provides
    ApptransidLogStore.Repository provideApptransidLogRepository(ApptransidLogStore.LocalStorage localStorage,
                                                                 ApptransidLogEntityDataMapper mapper) {
        return new ApptransidLogRepository(localStorage, mapper);
    }

}

package vn.com.vng.zalopay.internal.di.modules;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.data.api.entity.mapper.ApptransidLogEntityDataMapper;
import vn.com.vng.zalopay.data.apptransidlog.ApptransidLogApiCallLocalStorage;
import vn.com.vng.zalopay.data.apptransidlog.ApptransidLogLocalStorage;
import vn.com.vng.zalopay.data.apptransidlog.ApptransidLogRepository;
import vn.com.vng.zalopay.data.apptransidlog.ApptransidLogStore;
import vn.com.vng.zalopay.data.apptransidlog.ApptransidLogTimingLocalStorage;
import vn.com.vng.zalopay.data.cache.global.DaoSession;

/**
 * Created by hieuvm on 5/10/17.
 * *
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
    ApptransidLogStore.TimingLocalStorage provideApptransidLogTimingLocalStorage(@Named("globaldaosession") DaoSession session) {
        return new ApptransidLogTimingLocalStorage(session);
    }

    @Singleton
    @Provides
    ApptransidLogStore.ApiCallLocalStorage provideApptransidLogApiCallLocalStorage(@Named("globaldaosession") DaoSession session) {
        return new ApptransidLogApiCallLocalStorage(session);
    }

    @Singleton
    @Provides
    ApptransidLogStore.Repository provideApptransidLogRepository(ApptransidLogStore.LocalStorage localStorage,
                                                                 ApptransidLogStore.TimingLocalStorage timingLocalStorage,
                                                                 ApptransidLogStore.ApiCallLocalStorage apiCallLocalStorage,
                                                                 ApptransidLogEntityDataMapper mapper) {
        return new ApptransidLogRepository(localStorage, timingLocalStorage, apiCallLocalStorage, mapper);
    }

}
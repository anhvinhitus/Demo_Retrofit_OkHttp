package vn.com.vng.zalopay.internal.di.modules;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import vn.com.vng.zalopay.data.api.entity.mapper.ApptransidLogEntityDataMapper;
import vn.com.vng.zalopay.data.apptransidlog.ApptransidLogLocalStorage;
import vn.com.vng.zalopay.data.apptransidlog.ApptransidLogRepository;
import vn.com.vng.zalopay.data.apptransidlog.ApptransidLogStore;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.internal.di.scope.UserScope;

/**
 * Created by khattn on 1/24/17.
 */

@Module
public class UserApptransidLogModule {
    @UserScope
    @Provides
    ApptransidLogStore.LocalStorage provideApptransidLogLocalStorage(@Named("daosession") DaoSession session) {
        return new ApptransidLogLocalStorage(session);
    }

    @UserScope
    @Provides
    ApptransidLogStore.Repository provideApptransidLogRepository(ApptransidLogStore.RequestService requestService,
                                                                 ApptransidLogStore.LocalStorage localStorage,
                                                                 ApptransidLogEntityDataMapper mapper) {
        return new ApptransidLogRepository(requestService, localStorage, mapper);
    }

    @Provides
    @UserScope
    ApptransidLogStore.RequestService providesApptransidLogService(@Named("retrofitRedPacketApi") Retrofit retrofit) {
        return retrofit.create(ApptransidLogStore.RequestService.class);
    }
}

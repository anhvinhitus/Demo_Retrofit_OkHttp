package vn.com.vng.zalopay.internal.di.modules.user;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.data.cache.NotificationLocalStorage;
import vn.com.vng.zalopay.data.cache.NotificationStore;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.repository.NotificationRepository;
import vn.com.vng.zalopay.internal.di.scope.UserScope;

/**
 * Created by AnhHieu on 6/20/16.
 */
@Module
public class NotificationModule {

    @UserScope
    @Provides
    NotificationStore.LocalStorage provideNotificationLocalStorage(@Named("daosession") DaoSession session) {
        return new NotificationLocalStorage(session);
    }

    @UserScope
    @Provides
    NotificationStore.Repository providesNotificationRespository(NotificationStore.LocalStorage storage) {
        return new NotificationRepository(storage);
    }


   /* @Provides
    @UserScope
    NotificationStore.RequestService providesNotificationStoreService(@Named("retrofit") Retrofit retrofit) {
        return null;
    }*/
}

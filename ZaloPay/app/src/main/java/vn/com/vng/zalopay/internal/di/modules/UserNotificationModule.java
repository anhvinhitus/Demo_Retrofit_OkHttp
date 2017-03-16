package vn.com.vng.zalopay.internal.di.modules;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.notification.NotificationLocalStorage;
import vn.com.vng.zalopay.data.notification.NotificationRepository;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.rxbus.RxBus;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.scope.UserScope;

/**
 * Created by AnhHieu on 6/20/16.
 * Provide glue for notification module
 */
@Module
public class UserNotificationModule {

    @UserScope
    @Provides
    NotificationStore.LocalStorage provideNotificationLocalStorage(@Named("daosession") DaoSession session) {
        return new NotificationLocalStorage(session);
    }

    @UserScope
    @Provides
    NotificationStore.RequestService provideNotificationRequestService(@Named("retrofitConnector") Retrofit retrofit) {
        return retrofit.create(NotificationStore.RequestService.class);
    }

    @UserScope
    @Provides
    NotificationStore.Repository providesNotificationRepository(NotificationStore.LocalStorage storage,
                                                                EventBus eventBus,
                                                                RxBus rxBus,
                                                                NotificationStore.RequestService requestService,
                                                                User user
    ) {
        return new NotificationRepository(storage, eventBus, rxBus, requestService, user);
    }
}

package vn.com.vng.zalopay.internal.di.modules;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.notification.NotificationLocalStorage;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.notification.NotificationRepository;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.notification.NotificationHelper;

/**
 * Created by AnhHieu on 6/20/16.
 */
@Module
public class UserNotificationModule {

    @UserScope
    @Provides
    NotificationStore.LocalStorage provideNotificationLocalStorage(@Named("daosession") DaoSession session, User user, EventBus eventBus) {
        return new NotificationLocalStorage(session, user, eventBus);
    }

    @UserScope
    @Provides
    NotificationStore.Repository providesNotificationRespository(NotificationStore.LocalStorage storage) {
        return new NotificationRepository(storage);
    }

    @UserScope
    @Provides
    NotificationHelper providesNotificationHelper(Context context, NotificationStore.LocalStorage localStorage, AccountStore.Repository repository) {
        return new NotificationHelper(context, localStorage, repository);
    }


   /* @Provides
    @UserScope
    NotificationStore.RequestService providesNotificationStoreService(@Named("retrofit") Retrofit retrofit) {
        return null;
    }*/
}

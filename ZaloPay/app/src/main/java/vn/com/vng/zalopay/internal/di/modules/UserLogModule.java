package vn.com.vng.zalopay.internal.di.modules;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.data.ApptransidLogLocalStorage;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.internal.di.scope.UserScope;

/**
 * Created by khattn on 1/24/17.
 */

@Module
public class UserLogModule {
    @UserScope
    @Provides
    ApptransidLogLocalStorage provideLogLocalStorage(@Named("daosession") DaoSession session) {
        return new ApptransidLogLocalStorage(session);
    }
}

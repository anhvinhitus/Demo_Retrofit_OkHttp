package vn.com.vng.zalopay.internal.di.modules.user;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.transfer.TransferLocalStorage;
import vn.com.vng.zalopay.data.transfer.TransferStore;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.scope.UserScope;

@Module
public class UserModule {

    private final User user;

    public UserModule(User user) {
        this.user = user;
    }

    @Provides
    @UserScope
    User provideUser() {
        return user;
    }


    @Provides
    @UserScope
    TransferStore.LocalStorage provideTransferLocalStorage(@Named("daosession") DaoSession session) {
        return new TransferLocalStorage(session);
    }
}
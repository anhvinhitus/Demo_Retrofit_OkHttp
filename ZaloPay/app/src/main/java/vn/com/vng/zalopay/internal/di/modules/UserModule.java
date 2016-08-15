package vn.com.vng.zalopay.internal.di.modules;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.transfer.TransferLocalStorage;
import vn.com.vng.zalopay.data.transfer.TransferRepository;
import vn.com.vng.zalopay.data.transfer.TransferStore;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.scope.UserScope;

import com.zalopay.apploader.ReactNativeHostable;
import com.zalopay.apploader.ReactNativeHostLongLife;

@Module
public class UserModule {

    private final User user;

    public UserModule(User user) {
        Timber.d("Create new instance of UserModule");
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

    @Provides
    @UserScope
    TransferStore.Repository provideTransferRepository(TransferStore.LocalStorage localStorage) {
        return new TransferRepository(localStorage);
    }


    @Provides
    @UserScope
    ReactNativeHostable provideReactNativeInstanceManager() {
        Timber.d("Create new instance of ReactNativeInstanceManagerLongLife");
        return new ReactNativeHostLongLife();
    }
}
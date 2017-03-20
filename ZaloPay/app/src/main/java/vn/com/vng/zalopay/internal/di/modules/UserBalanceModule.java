package vn.com.vng.zalopay.internal.di.modules;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import vn.com.vng.zalopay.data.balance.BalanceLocalStorage;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.balance.BalanceRepository;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.scope.UserScope;

/**
 * Created by huuhoa on 6/15/16.
 * Provide glue on balance repository, balance local storage, balance request service
 */
@Module
public class UserBalanceModule {

    @UserScope
    @Provides
    BalanceStore.Repository provideBalanceRepository(
            BalanceStore.LocalStorage localStorage,
            BalanceStore.RequestService requestService,
            User user,
            EventBus eventBus) {
        return new BalanceRepository(localStorage, requestService, user, eventBus);
    }

    @UserScope
    @Provides
    BalanceStore.LocalStorage provideBalanceLocalStorage(@Named("daosession") DaoSession session) {
        return new BalanceLocalStorage(session);
    }

    @Provides
    @UserScope
    BalanceStore.RequestService provideBalanceRequestService(@Named("retrofitConnector") Retrofit retrofit) {
        return retrofit.create(BalanceStore.RequestService.class);
    }
}

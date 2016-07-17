package vn.com.vng.zalopay.internal.di.modules.user;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import vn.com.vng.zalopay.data.api.entity.mapper.ZaloPayEntityDataMapper;
import vn.com.vng.zalopay.data.cache.SqlZaloPayScope;
import vn.com.vng.zalopay.data.transaction.TransactionLocalStorage;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.transaction.TransactionRepository;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.scope.UserScope;

/**
 * Created by huuhoa on 6/15/16.
 * Provide glue on transaction local storage, transaction request service, transaction repository
 */
@Module
public class TransactionModule {
    @UserScope
    @Provides
    TransactionStore.Repository provideTransactionRepository(ZaloPayEntityDataMapper zaloPayEntityDataMapper,
                                                             User user,
                                                             SqlZaloPayScope sqlZaloPayScope,
                                                             TransactionStore.LocalStorage transactionLocalStorage,
                                                             TransactionStore.RequestService transactionRequestService) {
        return new TransactionRepository(zaloPayEntityDataMapper, user, sqlZaloPayScope, transactionLocalStorage, transactionRequestService);
    }


    @UserScope
    @Provides
    TransactionStore.LocalStorage provideTransactionLocalStorage(@Named("daosession") DaoSession session) {
        return new TransactionLocalStorage(session);
    }

    @Provides
    @UserScope
    TransactionStore.RequestService provideTransactionRequestService(@Named("retrofit") Retrofit retrofit) {
        return retrofit.create(TransactionStore.RequestService.class);
    }
}

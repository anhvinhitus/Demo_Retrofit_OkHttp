package vn.com.vng.zalopay.internal.di.modules.user;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.data.cache.TransactionLocalStorage;
import vn.com.vng.zalopay.data.cache.TransactionStore;
import vn.com.vng.zalopay.data.cache.mapper.ZaloPayDaoMapper;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.internal.di.scope.UserScope;

/**
 * Created by huuhoa on 6/15/16.
 * Provide glue on transaction local storage, transaction request service, transaction repository
 */
@Module
public class TransactionModule {
    @UserScope
    @Provides
    TransactionStore.LocalStorage provideTransactionLocalStorage(@Named("daosession") DaoSession session, ZaloPayDaoMapper zaloPayCacheMapper) {
        return new TransactionLocalStorage(session, zaloPayCacheMapper);
    }
}

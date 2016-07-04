package vn.com.vng.zalopay.internal.di.modules.user;

import android.content.Context;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.data.api.AppConfigService;
import vn.com.vng.zalopay.data.api.ZaloPayIAPService;
import vn.com.vng.zalopay.data.api.ZaloPayService;
import vn.com.vng.zalopay.data.api.entity.mapper.AppConfigEntityDataMapper;
import vn.com.vng.zalopay.data.api.entity.mapper.ZaloPayEntityDataMapper;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.SqlZaloPayScope;
import vn.com.vng.zalopay.data.cache.SqlZaloPayScopeImpl;
import vn.com.vng.zalopay.data.cache.SqlitePlatformScope;
import vn.com.vng.zalopay.data.cache.SqlitePlatformScopeImpl;
import vn.com.vng.zalopay.data.cache.mapper.PlatformDaoMapper;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.repository.AppConfigRepositoryImpl;
import vn.com.vng.zalopay.data.repository.ZaloPayIAPRepositoryImpl;
import vn.com.vng.zalopay.data.repository.ZaloPayRepositoryImpl;
import vn.com.vng.zalopay.data.repository.datasource.AppConfigFactory;
import vn.com.vng.zalopay.data.repository.datasource.ZaloPayFactory;
import vn.com.vng.zalopay.data.repository.datasource.ZaloPayIAPFactory;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.AppConfigRepository;
import vn.com.vng.zalopay.domain.repository.ZaloPayIAPRepository;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.mdl.IPaymentService;
import vn.com.vng.zalopay.service.PaymentServiceImpl;
import vn.com.vng.zalopay.transfer.ZaloFriendsFactory;

/**
 * Created by AnhHieu on 4/28/16.
 * User controller module
 */
@Module
public class UserControllerModule {

    @UserScope
    @Provides
    SqlitePlatformScope provideSqlitePlatformScope(@Named("daosession") DaoSession session, PlatformDaoMapper mapper) {
        return new SqlitePlatformScopeImpl(session, mapper);
    }


    @UserScope
    @Provides
    AppConfigFactory provideAppConfigFactory(Context context,
                                             AppConfigService service,
                                             User user,
                                             SqlitePlatformScope sqlitePlatformScope) {

        return new AppConfigFactory(context, service,
                user,
                sqlitePlatformScope);
    }

    @UserScope
    @Provides
    AppConfigRepository provideAppConfigRepository(AppConfigFactory appConfigFactory, AppConfigEntityDataMapper mapper) {
        return new AppConfigRepositoryImpl(appConfigFactory, mapper);
    }

    @UserScope
    @Provides
    SqlZaloPayScope provideSqlZaloPayScope(User user, @Named("daosession") DaoSession session) {
        return new SqlZaloPayScopeImpl(user, session);
    }

    @UserScope
    @Provides
    ZaloPayFactory provideZaloPayFactory(Context context, ZaloPayService service, User user) {
        return new ZaloPayFactory(context, service, user);
    }

    @UserScope
    @Provides
    ZaloPayRepository provideZaloPayRepository(ZaloPayFactory zaloPayFactory, ZaloPayEntityDataMapper mapper) {
        return new ZaloPayRepositoryImpl(zaloPayFactory, mapper);
    }

    @UserScope
    @Provides
    ZaloPayIAPFactory providesZaloPayIAPFactory(ZaloPayIAPService service, User user) {
        return new ZaloPayIAPFactory(service, user);
    }

    @UserScope
    @Provides
    ZaloPayIAPRepository providesZaloPayIAPRepository(ZaloPayIAPFactory factory) {
        return new ZaloPayIAPRepositoryImpl(factory);
    }

    @UserScope
    @Provides
    IPaymentService providesIPaymentService(ZaloPayIAPRepository zaloPayIAPRepository,
                                            BalanceStore.Repository balanceRepository,
                                            User user,
                                            TransactionStore.Repository transactionRepository) {
        return new PaymentServiceImpl(zaloPayIAPRepository, balanceRepository, user, transactionRepository);
    }

    @UserScope
    @Provides
    ZaloFriendsFactory providesZaloFriendsFactory(SqlZaloPayScope sqlZaloPayScope) {
        return new ZaloFriendsFactory(sqlZaloPayScope);
    }

}

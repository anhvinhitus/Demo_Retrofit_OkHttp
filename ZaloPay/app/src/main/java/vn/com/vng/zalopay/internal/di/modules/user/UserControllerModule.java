package vn.com.vng.zalopay.internal.di.modules.user;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.data.api.AccountService;
import vn.com.vng.zalopay.data.api.AppConfigService;
import vn.com.vng.zalopay.data.api.ZaloPayIAPService;
import vn.com.vng.zalopay.data.api.ZaloPayService;
import vn.com.vng.zalopay.data.api.entity.mapper.AppConfigEntityDataMapper;
import vn.com.vng.zalopay.data.api.entity.mapper.ZaloPayEntityDataMapper;
import vn.com.vng.zalopay.data.api.entity.mapper.ZaloPayIAPEntityDataMapper;
import vn.com.vng.zalopay.data.cache.BalanceStore;
import vn.com.vng.zalopay.data.cache.BalanceLocalStorage;
import vn.com.vng.zalopay.data.cache.SqlZaloPayScope;
import vn.com.vng.zalopay.data.cache.SqlZaloPayScopeImpl;
import vn.com.vng.zalopay.data.cache.SqlitePlatformScope;
import vn.com.vng.zalopay.data.cache.SqlitePlatformScopeImpl;
import vn.com.vng.zalopay.data.cache.TransactionStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.cache.mapper.PlatformDaoMapper;
import vn.com.vng.zalopay.data.cache.mapper.ZaloPayDaoMapper;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.download.DownloadAppResourceTaskQueue;
import vn.com.vng.zalopay.data.repository.AccountRepositoryImpl;
import vn.com.vng.zalopay.data.repository.AppConfigRepositoryImpl;
import vn.com.vng.zalopay.data.repository.BalanceRepositoryImpl;
import vn.com.vng.zalopay.data.repository.ZaloPayIAPRepositoryImpl;
import vn.com.vng.zalopay.data.repository.ZaloPayRepositoryImpl;
import vn.com.vng.zalopay.data.repository.datasource.AppConfigFactory;
import vn.com.vng.zalopay.data.repository.datasource.ZaloPayFactory;
import vn.com.vng.zalopay.data.repository.datasource.ZaloPayIAPFactory;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.AccountRepository;
import vn.com.vng.zalopay.domain.repository.AppConfigRepository;
import vn.com.vng.zalopay.domain.repository.BalanceRepository;
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
                                             SqlitePlatformScope sqlitePlatformScope,
                                             @Named("params_request_default") HashMap<String, String> params,
                                             DownloadAppResourceTaskQueue downloadQueue, OkHttpClient okHttpClient) {

        return new AppConfigFactory(context, service, user, sqlitePlatformScope, params, downloadQueue, okHttpClient, BuildConfig.DOWNLOAD_APP_RESOURCE);
    }

    @UserScope
    @Provides
    AppConfigRepository provideAppConfigRepository(AppConfigFactory appConfigFactory, AppConfigEntityDataMapper mapper) {
        return new AppConfigRepositoryImpl(appConfigFactory, mapper);
    }


/*    @UserScope
    @Provides
    SqlAppListScope provideSqlAppListScope(@Named("daosession") DaoSession session) {
        return new SqlAppListScopeImpl(session);
    }*/

    /* @UserScope
     @Provides
     AppListFactory provideAppListFactory(Context context, AppConfigService service,
                                          User user, SqlAppListScope sqlAppListScope) {
         return new AppListFactory(context, service, user, sqlAppListScope);
     }

     @UserScope
     @Provides
     ApplicationRepository provideApplicationRepository(AppListFactory appConfigFactory, ApplicationEntityDataMapper mapper) {
         return new ApplicationRepositoryImpl(appConfigFactory, mapper);
     }
 */
    @UserScope
    @Provides
    SqlZaloPayScope provideSqlZaloPayScope(User user, @Named("daosession") DaoSession session, ZaloPayDaoMapper zaloPayCacheMapper) {
        return new SqlZaloPayScopeImpl(user, session, zaloPayCacheMapper);
    }

    @UserScope
    @Provides
    ZaloPayFactory provideZaloPayFactory(Context context, ZaloPayService service,
                                         User user, SqlZaloPayScope sqlZaloPayScope,
                                         TransactionStore.LocalStorage transactionLocalStorage,
                                         @Named("payAppId") int payAppId, EventBus eventBus) {
        return new ZaloPayFactory(context, service, user, sqlZaloPayScope, transactionLocalStorage, payAppId, eventBus);
    }

    @UserScope
    @Provides
    ZaloPayRepository provideZaloPayRepository(ZaloPayFactory zaloPayFactory, ZaloPayEntityDataMapper mapper) {
        return new ZaloPayRepositoryImpl(zaloPayFactory, mapper);
    }

    @UserScope
    @Provides
    AccountRepository provideAccountRepository(AccountService accountService, UserConfig userConfig) {
        return new AccountRepositoryImpl(accountService, userConfig);
    }

    @UserScope
    @Provides
    ZaloPayIAPFactory providesZaloPayIAPFactory(ZaloPayIAPService service, User user) {
        return new ZaloPayIAPFactory(service, user);
    }

    @UserScope
    @Provides
    ZaloPayIAPRepository providesZaloPayIAPRepository(ZaloPayIAPFactory factory, ZaloPayFactory zaloPayFactory, ZaloPayIAPEntityDataMapper mapper) {
        return new ZaloPayIAPRepositoryImpl(factory, zaloPayFactory, mapper);
    }

    @UserScope
    @Provides
    IPaymentService providesIPaymentService(ZaloPayIAPRepository zaloPayIAPRepository, BalanceRepository balanceRepository, User user) {
        return new PaymentServiceImpl(zaloPayIAPRepository, balanceRepository, user);
    }

    @UserScope
    @Provides
    ZaloFriendsFactory providesZaloFriendsFactory(SqlZaloPayScope sqlZaloPayScope) {
        return new ZaloFriendsFactory(sqlZaloPayScope);
    }

}

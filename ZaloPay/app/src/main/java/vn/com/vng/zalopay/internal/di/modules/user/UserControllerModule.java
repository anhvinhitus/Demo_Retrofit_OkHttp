package vn.com.vng.zalopay.internal.di.modules.user;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import vn.com.vng.zalopay.data.api.AppConfigService;
import vn.com.vng.zalopay.data.api.ZaloPayService;
import vn.com.vng.zalopay.data.api.entity.mapper.AppConfigEntityDataMapper;
import vn.com.vng.zalopay.data.api.entity.mapper.ApplicationEntityDataMapper;
import vn.com.vng.zalopay.data.api.entity.mapper.ZaloPayEntityDataMapper;
import vn.com.vng.zalopay.data.cache.SqlAppListScope;
import vn.com.vng.zalopay.data.cache.SqlAppListScopeImpl;
import vn.com.vng.zalopay.data.cache.SqlZaloPayScope;
import vn.com.vng.zalopay.data.cache.SqlZaloPayScopeImpl;
import vn.com.vng.zalopay.data.cache.SqlitePlatformScope;
import vn.com.vng.zalopay.data.cache.SqlitePlatformScopeImpl;
import vn.com.vng.zalopay.data.cache.mapper.PlatformDaoMapper;
import vn.com.vng.zalopay.data.cache.mapper.ZaloPayDaoMapper;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.download.DownloadAppResourceTaskQueue;
import vn.com.vng.zalopay.data.repository.AppConfigRepositoryImpl;
import vn.com.vng.zalopay.data.repository.ApplicationRepositoryImpl;
import vn.com.vng.zalopay.data.repository.ZaloPayRepositoryImpl;
import vn.com.vng.zalopay.data.repository.datasource.AppConfigFactory;
import vn.com.vng.zalopay.data.repository.datasource.AppListFactory;
import vn.com.vng.zalopay.data.repository.datasource.ZaloPayFactory;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.AppConfigRepository;
import vn.com.vng.zalopay.domain.repository.ApplicationRepository;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.internal.di.scope.UserScope;

/**
 * Created by AnhHieu on 4/28/16.
 *
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

        return new AppConfigFactory(context, service, user, sqlitePlatformScope, params, downloadQueue, okHttpClient);
    }

    @UserScope
    @Provides
    AppConfigRepository provideAppConfigRepository(AppConfigFactory appConfigFactory, AppConfigEntityDataMapper mapper) {
        return new AppConfigRepositoryImpl(appConfigFactory, mapper);
    }


    @UserScope
    @Provides
    SqlAppListScope provideSqlAppListScope(@Named("daosession") DaoSession session) {
        return new SqlAppListScopeImpl(session);
    }

    @UserScope
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

    @UserScope
    @Provides
    SqlZaloPayScope provideSqlZaloPayScope(User user, @Named("daosession") DaoSession session, ZaloPayDaoMapper zaloPayCacheMapper) {
        return new SqlZaloPayScopeImpl(user, session, zaloPayCacheMapper);
    }

    @UserScope
    @Provides
    ZaloPayFactory provideZaloPayFactory(Context context, ZaloPayService service,
                                         User user, SqlZaloPayScope sqlZaloPayScope, @Named("payAppId") int payAppId, EventBus eventBus) {
        return new ZaloPayFactory(context, service, user, sqlZaloPayScope, payAppId, eventBus);
    }

    @UserScope
    @Provides
    ZaloPayRepository provideZaloPayRepository(ZaloPayFactory zaloPayFactory, ZaloPayEntityDataMapper mapper) {
        return new ZaloPayRepositoryImpl(zaloPayFactory, mapper);
    }


}

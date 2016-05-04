package vn.com.vng.zalopay.internal.di.modules.user;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.data.api.AppConfigService;
import vn.com.vng.zalopay.data.api.ParamRequestProvider;
import vn.com.vng.zalopay.data.api.entity.mapper.ApplicationEntityDataMapper;
import vn.com.vng.zalopay.data.cache.SqlAppListScope;
import vn.com.vng.zalopay.data.cache.SqlAppListScopeImpl;
import vn.com.vng.zalopay.data.cache.SqlitePlatformScope;
import vn.com.vng.zalopay.data.cache.SqlitePlatformScopeImpl;
import vn.com.vng.zalopay.data.repository.AppConfigRepositoryImpl;
import vn.com.vng.zalopay.data.repository.ApplicationRepositoryImpl;
import vn.com.vng.zalopay.data.repository.datasource.AppConfigFactory;
import vn.com.vng.zalopay.data.repository.datasource.AppListFactory;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.AppConfigRepository;
import vn.com.vng.zalopay.domain.repository.ApplicationRepository;
import vn.com.vng.zalopay.internal.di.scope.UserScope;

/**
 * Created by AnhHieu on 4/28/16.
 */
@Module
public class UserControllerModule {

    @UserScope
    @Provides
    SqlitePlatformScope provideSqlitePlatformScope() {
        return new SqlitePlatformScopeImpl();
    }


    @UserScope
    @Provides
    AppConfigFactory provideAppConfigFactory(Context context, AppConfigService service, ParamRequestProvider paramRequestProvider,
                                             User user, SqlitePlatformScope sqlitePlatformScope) {
        return new AppConfigFactory(context, service, paramRequestProvider, user, sqlitePlatformScope);
    }

    @UserScope
    @Provides
    AppConfigRepository provideAppConfigRepository(AppConfigFactory appConfigFactory) {
        return new AppConfigRepositoryImpl(appConfigFactory);
    }


    @UserScope
    @Provides
    SqlAppListScope provideSqlAppListScope() {
        return new SqlAppListScopeImpl();
    }

    @UserScope
    @Provides
    AppListFactory provideAppListFactory(Context context, AppConfigService service, ParamRequestProvider paramRequestProvider,
                                         User user, SqlAppListScope sqlAppListScope) {
        return new AppListFactory(context, service, paramRequestProvider, user, sqlAppListScope);
    }

    @UserScope
    @Provides
    ApplicationRepository provideApplicationRepository(AppListFactory appConfigFactory, ApplicationEntityDataMapper mapper) {
        return new ApplicationRepositoryImpl(appConfigFactory, mapper);
    }

}

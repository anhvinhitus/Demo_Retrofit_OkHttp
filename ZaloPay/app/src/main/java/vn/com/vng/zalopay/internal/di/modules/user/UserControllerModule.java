package vn.com.vng.zalopay.internal.di.modules.user;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.data.api.AppConfigService;
import vn.com.vng.zalopay.data.api.ParamRequestProvider;
import vn.com.vng.zalopay.data.cache.SqlitePlatformScope;
import vn.com.vng.zalopay.data.cache.SqlitePlatformScopeImpl;
import vn.com.vng.zalopay.data.repository.AppConfigRepositoryImpl;
import vn.com.vng.zalopay.data.repository.datasource.AppConfigFactory;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.AppConfigRepository;
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


}

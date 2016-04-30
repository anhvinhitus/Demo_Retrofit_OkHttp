package vn.com.vng.zalopay.internal.di.modules.user;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.data.repository.AppConfigRepositoryImpl;
import vn.com.vng.zalopay.data.repository.datasource.AppConfigFactory;
import vn.com.vng.zalopay.domain.repository.AppConfigRepository;
import vn.com.vng.zalopay.internal.di.scope.UserScope;

/**
 * Created by AnhHieu on 4/28/16.
 */
@Module
public class UserControllerModule {

    @UserScope
    @Provides
    AppConfigFactory provideAppConfigFactory(AppConfigFactory factory) {
        return factory;
    }

    @UserScope
    @Provides
    AppConfigRepository provideAppConfigRepository(AppConfigFactory appConfigFactory) {
        return new AppConfigRepositoryImpl(appConfigFactory);
    }


}

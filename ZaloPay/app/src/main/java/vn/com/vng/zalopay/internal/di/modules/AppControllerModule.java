package vn.com.vng.zalopay.internal.di.modules;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.data.repository.PassportRepositoryImpl;
import vn.com.vng.zalopay.domain.repository.PassportRepository;

/**
 * Created by AnhHieu on 4/2/16.
 */

@Module
public class AppControllerModule {

    public AppControllerModule() {
    }

    @Provides
    @Singleton
    PassportRepository providePassportRepository(PassportRepositoryImpl passportRepository) {
        return passportRepository;
    }

}

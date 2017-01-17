package vn.com.vng.zalopay.internal.di.modules;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.repository.PassportRepositoryImpl;
import vn.com.vng.zalopay.data.zalosdk.ZaloSdkApi;
import vn.com.vng.zalopay.domain.executor.ThreadExecutor;
import vn.com.vng.zalopay.domain.repository.PassportRepository;
import vn.com.vng.zalopay.zalosdk.ZaloSDKApiImpl;

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


    @Provides
    ZaloSdkApi providesZaloSdkApi(Context context, UserConfig userConfig, ThreadExecutor threadExecutor) {
        return new ZaloSDKApiImpl(context, userConfig, threadExecutor);
    }
}

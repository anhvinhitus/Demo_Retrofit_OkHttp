package vn.com.zalopay.wallet.di.component;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Component;
import vn.com.zalopay.wallet.configure.SDKConfiguration;
import vn.com.zalopay.wallet.datasource.InjectionWrapper;
import vn.com.zalopay.wallet.di.module.AppInfoRepositoryModule;
import vn.com.zalopay.wallet.di.module.ApplicationModule;
import vn.com.zalopay.wallet.di.module.BankListRepositoryModule;
import vn.com.zalopay.wallet.di.module.ConfigurationModule;
import vn.com.zalopay.wallet.di.module.InteractorModule;
import vn.com.zalopay.wallet.di.module.PlatformInfoRepositoryModule;
import vn.com.zalopay.wallet.interactor.IAppInfo;
import vn.com.zalopay.wallet.interactor.IBankList;
import vn.com.zalopay.wallet.interactor.IPlatformInfo;

@Singleton
@Component(modules = {ApplicationModule.class,
        ConfigurationModule.class,
        AppInfoRepositoryModule.class,
        BankListRepositoryModule.class,
        PlatformInfoRepositoryModule.class,
        InteractorModule.class})
public interface ApplicationComponent {

    void inject(InjectionWrapper pInjectionWrapper);

    Application application();

    SDKConfiguration sdkConfiguration();

    IPlatformInfo platformInfoInteractor();

    IBankList bankListInteractor();

    IAppInfo appInfoInteractor();
}

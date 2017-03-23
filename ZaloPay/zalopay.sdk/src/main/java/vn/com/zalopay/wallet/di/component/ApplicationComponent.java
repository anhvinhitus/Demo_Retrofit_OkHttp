package vn.com.zalopay.wallet.di.component;

import javax.inject.Singleton;

import dagger.Component;
import vn.com.zalopay.wallet.di.module.ApplicationModule;
import vn.com.zalopay.wallet.di.module.ServiceModule;

@Singleton
@Component(modules = {ApplicationModule.class, ServiceModule.class})
public interface ApplicationComponent {

    //void inject(DataRepository pDataRepository);
}

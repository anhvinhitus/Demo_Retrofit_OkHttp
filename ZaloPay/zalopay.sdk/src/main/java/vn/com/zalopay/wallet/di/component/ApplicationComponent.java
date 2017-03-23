package vn.com.zalopay.wallet.di.component;

import javax.inject.Singleton;

import dagger.Component;
import vn.com.zalopay.wallet.datasource.DataRepository;
import vn.com.zalopay.wallet.di.module.AppModule;
import vn.com.zalopay.wallet.di.module.ServiceModule;

@Singleton
@Component(modules = {AppModule.class, ServiceModule.class})
public interface AppComponent {

    //void inject(DataRepository pDataRepository);
}

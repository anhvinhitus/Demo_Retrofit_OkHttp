package vn.com.zalopay.wallet.di.component;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Component;
import vn.com.zalopay.wallet.configure.SDKConfiguration;
import vn.com.zalopay.wallet.datasource.InjectionWrapper;
import vn.com.zalopay.wallet.di.module.ApplicationModule;
import vn.com.zalopay.wallet.di.module.ConfigurationModule;
import vn.com.zalopay.wallet.di.module.PaymentSessionModule;

@Singleton
@Component(modules = {ApplicationModule.class, ConfigurationModule.class})
public interface ApplicationComponent {

    void inject(InjectionWrapper pInjectionWrapper);

    Application application();

    SDKConfiguration sdkConfiguration();

    PaymentSessionComponent plus(PaymentSessionModule pPaymentSessionModule);
}

package vn.com.vng.zalopay.internal.di.modules.user;

import android.app.Application;
import android.content.Context;

import com.facebook.react.LifecycleState;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModuleCallExceptionHandler;
import com.facebook.react.shell.MainReactPackage;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.BundleReactConfig;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.mdl.BundleService;
import vn.com.vng.zalopay.mdl.impl.BundleServiceImpl;
import vn.com.vng.zalopay.mdl.internal.ReactIAPPackage;
import vn.com.vng.zalopay.mdl.internal.ReactInternalPackage;

/**
 * Created by AnhHieu on 5/12/16.
 */
@Module
public class ReactNativeModule {

    //Todo : heavy process
    @UserScope
    @Provides
    @Named("bundleservice")
    BundleService providesBundleService(Context context) {
        BundleServiceImpl bundleService = new BundleServiceImpl((Application) context);
        bundleService.prepareInternalBundle();
        Timber.d("internalBundle %s", bundleService.mCurrentInternalBundleFolder);
        return bundleService;
    }


    @UserScope
    @Provides
    @Named("internalBundle")
    String providesInternalBundleFolder(@Named("bundleservice") BundleService service) {
        return service.getInternalBundleFolder();
    }


    @UserScope
    @Provides
    @Named("reactMainPackage")
    ReactPackage provideMainReactPackage() {
        return new MainReactPackage();
    }

    @UserScope
    @Provides
    @Named("reactInternalPackage")
    ReactPackage provideReactInternalPackage(ZaloPayRepository repository) {
        return new ReactInternalPackage(repository);
    }

    @UserScope
    @Provides
    @Named("reactIAPPackage")
    ReactPackage provideReactIAPPackage(ZaloPayRepository repository, User user) {
        return new ReactIAPPackage(repository, user);
    }

    @UserScope
    @Provides
    BundleReactConfig provideBundleReactConfig(Context context, @Named("bundleservice") BundleService service) {
        return new BundleReactConfig((Application) context, service);
    }

    @UserScope
    @Provides
    ReactInstanceManager providesReactInstanceManager(Context context, @Named("internalBundle") String internalBundle,
                                                      @Named("reactInternalPackage") ReactPackage internal,
                                                      @Named("reactMainPackage") ReactPackage main,
                                                      @Named("reactIAPPackage") ReactPackage iapPackage) {
        Timber.d("providesReactInstanceManager %s, %s", internalBundle, BuildConfig.DEBUG);
        return ReactInstanceManager.builder()
                .setApplication((Application) context)
//                .setJSBundleFile(internalBundle + "/main.jsbundle")
//                .setBundleAssetName("index.android.bundle")
                .setJSMainModuleName("index.android")
                .addPackage(main)
                .addPackage(internal)
                .addPackage(iapPackage)
                .setUseDeveloperSupport(BuildConfig.DEBUG)
                .setInitialLifecycleState(LifecycleState.RESUMED)
                .setNativeModuleCallExceptionHandler(new NativeModuleCallExceptionHandler() {
                    @Override
                    public void handleException(Exception e) {
                        Timber.e(e, "Error from React Native module");
                    }
                })
                .build();
    }


   /* @UserScope
    @Provides
    BundleWrapper providesBundleWrapper(Context context) {
        return new BundleWrapper((Application) context);
    }*/


}

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
import vn.com.vng.zalopay.config.ReactSupport;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.mdl.BundleReactConfig;
import vn.com.vng.zalopay.mdl.BundleService;
import vn.com.vng.zalopay.mdl.impl.BundleReactConfigDevel;
import vn.com.vng.zalopay.mdl.impl.BundleReactConfigImpl;
import vn.com.vng.zalopay.mdl.impl.BundleReactConfigRelease;
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
    BundleReactConfig provideBundleReactConfig(Context context, @Named("bundleservice") BundleService service) {
        switch (BuildConfig.REACT_DEVELOP_SUPPORT) {
            case DEV_INTERNAL:
                return new BundleReactConfigDevel();
            case DEV_EXTERNAL:
                return new BundleReactConfigImpl(service);
            case RELEASE:
                return new BundleReactConfigRelease(service);
            default:
                return null;
        }
    }
}

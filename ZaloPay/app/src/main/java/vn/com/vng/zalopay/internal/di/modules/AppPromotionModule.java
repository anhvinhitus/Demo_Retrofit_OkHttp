package vn.com.vng.zalopay.internal.di.modules;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.promotion.PromotionHelper;
import vn.com.vng.zalopay.promotion.ResourceLoader;
import vn.zalopay.promotion.IResourceLoader;

/**
 * Created by chucvv on 5/27/17.
 */

@Module
public class AppPromotionModule {
    @Provides
    @Singleton
    IResourceLoader providePromotionResourceLoader() {
        return new ResourceLoader();
    }

    @Provides
    @Singleton
    PromotionHelper providePromotionHelper(Navigator navigator) {
        return new PromotionHelper(navigator);
    }
}

package vn.com.vng.zalopay.internal.di.modules;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.promotion.PromotionHelper;
import vn.com.vng.zalopay.promotion.ResourceLoader;
import vn.com.vng.zalopay.utils.ImageLoader;

/**
 * Created by chucvv on 5/27/17.
 */

@Module
public class AppPromotionModule {
    @Provides
    @Singleton
    ResourceLoader providePromotionResourceLoader(ImageLoader imageLoader) {
        return new ResourceLoader(imageLoader);
    }

    @Provides
    @Singleton
    PromotionHelper providePromotionHelper(Navigator navigator) {
        return new PromotionHelper(navigator);
    }
}

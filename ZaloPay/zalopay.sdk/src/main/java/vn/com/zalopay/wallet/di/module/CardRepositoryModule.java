package vn.com.zalopay.wallet.di.module;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.di.qualifier.Api;
import vn.com.zalopay.wallet.repository.cardmap.CardLocalStorage;
import vn.com.zalopay.wallet.repository.cardmap.CardStore;

/**
 * Created by chucvv on 6/7/17.
 */
@Module
public class CardRepositoryModule {
    @Provides
    @Singleton
    public CardStore.CardMapService provideCardService(@Api Retrofit retrofit) {
        return retrofit.create(CardStore.CardMapService.class);
    }

    @Provides
    @Singleton
    public CardStore.LocalStorage provideCardLocalStorage(SharedPreferencesManager sharedPreferencesManager) {
        return new CardLocalStorage(sharedPreferencesManager);
    }
}

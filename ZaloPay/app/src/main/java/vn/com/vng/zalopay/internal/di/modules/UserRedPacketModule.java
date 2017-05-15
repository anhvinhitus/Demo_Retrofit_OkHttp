package vn.com.vng.zalopay.internal.di.modules;

import com.google.gson.Gson;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.redpacket.RedPacketLocalStorage;
import vn.com.vng.zalopay.data.redpacket.RedPacketRepository;
import vn.com.vng.zalopay.data.redpacket.RedPacketStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.react.redpacket.AlertDialogProvider;
import vn.com.vng.zalopay.react.redpacket.IRedPacketPayService;
import vn.com.vng.zalopay.service.RedPacketPayServiceImpl;
import vn.com.vng.zalopay.service.SweetAlertDialogImpl;

/**
 * Created by longlv on 13/07/2016.
 */
@Module
public class UserRedPacketModule {

    @UserScope
    @Provides
    RedPacketStore.LocalStorage provideRedPacketStorage(@Named("daosession") DaoSession session) {
        return new RedPacketLocalStorage(session);
    }

    @Provides
    @UserScope
    RedPacketStore.RequestService providesRedPacketService(@Named("retrofitRedPacketApi") Retrofit retrofit) {
        return retrofit.create(RedPacketStore.RequestService.class);
    }

    @Provides
    @UserScope
    RedPacketStore.RequestTPEService providesRedPacketTPEService(@Named("retrofitConnector") Retrofit retrofit) {
        return retrofit.create(RedPacketStore.RequestTPEService.class);
    }

    @UserScope
    @Provides
    RedPacketStore.Repository provideRedPacketRepository(RedPacketStore.RequestService requestService,
                                                         RedPacketStore.RequestTPEService requestTPEService,
                                                         RedPacketStore.LocalStorage localStorage,
                                                         User user, Gson gson) {
        return new RedPacketRepository(requestService, requestTPEService, localStorage, user, BuildConfig.ZALOPAY_APP_ID, gson);
    }

    @UserScope
    @Provides
    IRedPacketPayService providesIRedPacketPayService(BalanceStore.Repository balanceRepository,
                                                      TransactionStore.Repository transactionRepository) {
        return new RedPacketPayServiceImpl(balanceRepository, transactionRepository);
    }

    @UserScope
    @Provides
    AlertDialogProvider providesISweetAlertDialog() {
        return new SweetAlertDialogImpl();
    }
}

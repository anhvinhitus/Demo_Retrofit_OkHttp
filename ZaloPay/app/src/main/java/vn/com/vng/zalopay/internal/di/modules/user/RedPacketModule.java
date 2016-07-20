package vn.com.vng.zalopay.internal.di.modules.user;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import vn.com.vng.zalopay.data.api.entity.mapper.RedPacketDataMapper;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.redpacket.RedPackageLocalStorage;
import vn.com.vng.zalopay.data.redpacket.RedPackageRepositoryImpl;
import vn.com.vng.zalopay.data.redpacket.RedPacketStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.mdl.redpackage.IRedPacketPayService;
import vn.com.vng.zalopay.service.RedPacketPayServiceImpl;

/**
 * Created by longlv on 13/07/2016.
 *
 */
@Module
public class RedPacketModule {

    @UserScope
    @Provides
    RedPacketStore.LocalStorage provideRedPacketStorage(@Named("daosession") DaoSession session, RedPacketDataMapper dataMapper) {
        return new RedPackageLocalStorage(session, dataMapper);
    }

    @Provides
    @UserScope
    RedPacketStore.RequestService providesRedPacketService(@Named("retrofit") Retrofit retrofit) {
        return retrofit.create(RedPacketStore.RequestService.class);
    }

    @UserScope
    @Provides
    RedPacketStore.Repository provideRedPacketRepository(RedPacketStore.RequestService requestService, RedPacketStore.LocalStorage localStorage, RedPacketDataMapper dataMapper, UserConfig userConfig, User user) {
        return new RedPackageRepositoryImpl(requestService, localStorage, dataMapper, userConfig, user);
    }

    @UserScope
    @Provides
    IRedPacketPayService providesIRedPacketPayService(BalanceStore.Repository balanceRepository,
                                                      TransactionStore.Repository transactionRepository) {
        return new RedPacketPayServiceImpl(balanceRepository, transactionRepository);
    }
}

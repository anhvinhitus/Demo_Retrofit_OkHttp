package vn.com.vng.zalopay.internal.di.modules;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.notification.NotificationLocalStorage;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.notification.NotificationRepository;
import vn.com.vng.zalopay.data.redpacket.RedPacketStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.notification.NotificationHelper;

/**
 * Created by AnhHieu on 6/20/16.
 */
@Module
public class UserNotificationModule {

    @UserScope
    @Provides
    NotificationStore.LocalStorage provideNotificationLocalStorage(@Named("daosession") DaoSession session, User user) {
        return new NotificationLocalStorage(session, user);
    }

    @UserScope
    @Provides
    NotificationStore.Repository providesNotificationRepository(NotificationStore.LocalStorage storage, EventBus eventBus) {
        return new NotificationRepository(storage, eventBus);
    }

    @UserScope
    @Provides
    NotificationHelper providesNotificationHelper(Context context, User user,
                                                  AccountStore.Repository accountRepository,
                                                  NotificationStore.Repository notifyRepository,
                                                  RedPacketStore.Repository redPacketRepository,
                                                  TransactionStore.Repository transactionRepository,
                                                  BalanceStore.Repository balanceRepository
    ) {
        return new NotificationHelper(context, user,
                notifyRepository, accountRepository, redPacketRepository,
                transactionRepository, balanceRepository);
    }
}

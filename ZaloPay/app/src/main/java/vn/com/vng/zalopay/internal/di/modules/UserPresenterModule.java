package vn.com.vng.zalopay.internal.di.modules;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.account.ui.presenter.ChangePinPresenter;
import vn.com.vng.zalopay.account.ui.presenter.IChangePinPresenter;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.domain.repository.ZaloPayIAPRepository;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.presenter.ZaloPayPresenter;
import vn.com.vng.zalopay.ui.presenter.ZaloPayPresenterImpl;

@Module
public class UserPresenterModule {

    @UserScope
    @Provides
    IChangePinPresenter providesChangePinPresenter(Context context, AccountStore.Repository accountRepository) {
        return new ChangePinPresenter(context, accountRepository);
    }

  /*  @UserScope
    @Provides
    TransferMoneyPresenter provideTransferPresenter(User user, NotificationStore.Repository notificationRepository) {
        return new TransferPresenter(user, notificationRepository);
    }*/

    @UserScope
    @Provides
    ZaloPayPresenter providerZaloPayPresenter(ZaloPayIAPRepository zaloPayIAPRepository,
                                              EventBus eventBus,
                                              BalanceStore.Repository balanceRepository,
                                              AppResourceStore.Repository appResourceRepository,
                                              NotificationStore.Repository notificationRepository,
                                              Navigator navigator) {
        return new ZaloPayPresenterImpl(zaloPayIAPRepository,
                eventBus,
                balanceRepository,
                appResourceRepository,
                notificationRepository,
                navigator);
    }
}

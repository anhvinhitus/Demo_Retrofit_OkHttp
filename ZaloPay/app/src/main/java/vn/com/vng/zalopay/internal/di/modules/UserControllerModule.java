package vn.com.vng.zalopay.internal.di.modules;

import org.greenrobot.eventbus.EventBus;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.data.api.ZaloPayService;
import vn.com.vng.zalopay.data.api.entity.mapper.ZaloPayEntityDataMapper;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.merchant.MerchantStore;
import vn.com.vng.zalopay.data.repository.ZaloPayRepositoryImpl;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.react.iap.IPaymentService;
import vn.com.vng.zalopay.service.PaymentServiceImpl;

/**
 * Created by AnhHieu on 4/28/16.
 * User controller module
 */
@Module
public class UserControllerModule {
    @UserScope
    @Provides
    ZaloPayRepository provideZaloPayRepository(ZaloPayService service, User user, ZaloPayEntityDataMapper mapper) {
        return new ZaloPayRepositoryImpl(mapper, service, user);
    }

    @UserScope
    @Provides
    IPaymentService providesIPaymentService(MerchantStore.Repository merchantRepository,
                                            BalanceStore.Repository balanceRepository,
                                            TransactionStore.Repository transactionRepository,
                                            EventBus eventBus
    ) {
        return new PaymentServiceImpl(merchantRepository, balanceRepository, transactionRepository, eventBus);
    }
}

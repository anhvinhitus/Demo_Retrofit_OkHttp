package vn.com.zalopay.wallet.di.module;

import dagger.Module;
import dagger.Provides;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.di.scope.ScopePaymentSession;

@Module
public class PaymentSessionModule {
    protected ZPWPaymentInfo mPaymentInfo;

    public PaymentSessionModule(ZPWPaymentInfo pPaymentInfo) {
        this.mPaymentInfo = pPaymentInfo;
    }

    @Provides
    @ScopePaymentSession
    public ZPWPaymentInfo providePaymentInfo() {
        return this.mPaymentInfo;
    }
}

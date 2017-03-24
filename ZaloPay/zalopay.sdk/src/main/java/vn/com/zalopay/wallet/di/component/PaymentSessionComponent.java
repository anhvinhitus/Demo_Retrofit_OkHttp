package vn.com.zalopay.wallet.di.component;

import dagger.Subcomponent;
import vn.com.zalopay.wallet.business.data.PaymentSessionInfo;
import vn.com.zalopay.wallet.di.module.PaymentSessionModule;
import vn.com.zalopay.wallet.di.scope.ScopePaymentSession;

@ScopePaymentSession
@Subcomponent(modules = PaymentSessionModule.class)
public interface PaymentSessionComponent {
    void inject(PaymentSessionInfo pPaymentSessionInfo);
}

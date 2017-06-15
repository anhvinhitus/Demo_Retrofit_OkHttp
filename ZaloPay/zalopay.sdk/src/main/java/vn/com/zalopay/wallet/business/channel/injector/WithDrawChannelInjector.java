package vn.com.zalopay.wallet.business.channel.injector;

import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;

public class WithDrawChannelInjector extends BaseChannelInjector {
    public WithDrawChannelInjector(PaymentInfoHelper paymentInfoHelper) {
        super(paymentInfoHelper);
    }

    @Override
    protected void detectChannel() throws Exception {
        getMapBankAccount();
        getMapCard();
        source.onCompleted();
    }
}

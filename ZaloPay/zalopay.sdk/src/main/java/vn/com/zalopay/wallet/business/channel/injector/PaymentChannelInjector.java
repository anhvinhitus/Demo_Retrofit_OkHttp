package vn.com.zalopay.wallet.business.channel.injector;

import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;

public class PaymentChannelInjector extends BaseChannelInjector {
    public PaymentChannelInjector(PaymentInfoHelper paymentInfoHelper) {
        super(paymentInfoHelper);
    }

    @Override
    protected void detectChannel() throws Exception {
        getMapBankAccount();
        getMapCard();
        getChannelFromConfig();
        source.onCompleted();
    }
}

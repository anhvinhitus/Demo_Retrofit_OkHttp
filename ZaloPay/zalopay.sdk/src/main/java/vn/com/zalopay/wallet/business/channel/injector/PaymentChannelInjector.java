package vn.com.zalopay.wallet.business.channel.injector;

import vn.com.zalopay.wallet.listener.ZPWOnGetChannelListener;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;

public class PaymentChannelInjector extends BaseChannelInjector {
    public PaymentChannelInjector(PaymentInfoHelper paymentInfoHelper) {
        super(paymentInfoHelper);
    }

    @Override
    protected void detectChannel(ZPWOnGetChannelListener pListener) throws Exception {
        getMapBankAccount();
        getMapCard();
        getChannelFromConfig();
        sortChannels();
        if (pListener != null) {
            pListener.onGetChannelComplete();
        }

    }
}

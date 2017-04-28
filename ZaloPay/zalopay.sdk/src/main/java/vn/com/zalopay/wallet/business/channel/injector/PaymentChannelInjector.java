package vn.com.zalopay.wallet.business.channel.injector;

import vn.com.zalopay.wallet.listener.ZPWOnGetChannelListener;

public class PaymentChannelInjector extends BaseChannelInjector {
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
